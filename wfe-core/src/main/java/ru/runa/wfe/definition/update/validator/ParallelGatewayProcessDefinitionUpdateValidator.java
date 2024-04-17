package ru.runa.wfe.definition.update.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.definition.update.ProcessDefinitionUpdateData;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;

/**
 * Parallel gateway does not work in case of missed token in any of input transition. This validator prevents these errors.
 *
 * @author azyablin, kharo1963, dofs
 */
@Component
@CommonsLog
public class ParallelGatewayProcessDefinitionUpdateValidator implements ProcessDefinitionUpdateValidator {
    @Autowired
    CurrentTokenDao currentTokenDao;

    private static class NotCompatibleParallelGatewayInfo {
        private final String nodeId;
        private final String errorMessage;
        // узлы, которые могут активировать этот переход; сюда не включены общие узлы для всех переходов
        private final Map<String, Set<String>> transitionIdToPrecedessorNodeIds = new HashMap<>();
        // пересечение transitionIdToPrecedessorNodeIds
        private final Set<String> allPrecedessorNodeIds = new HashSet<>();

        public NotCompatibleParallelGatewayInfo(String nodeId, String errorMessage, Map<String, Set<Node>> transitionIdToPrecedessorNodes) {
            this.nodeId = nodeId;
            this.errorMessage = errorMessage;
            for (Map.Entry<String, Set<Node>> entry : transitionIdToPrecedessorNodes.entrySet()) {
                Set<String> nodeIds = entry.getValue().stream().map(n -> n.getNodeId()).collect(Collectors.toSet());
                transitionIdToPrecedessorNodeIds.put(entry.getKey(), nodeIds);
                allPrecedessorNodeIds.addAll(nodeIds);
            }
        }

    }

    @Override
    public void validate(ProcessDefinitionUpdateData processDefinitionUpdateData) {
        if (processDefinitionUpdateData.getNewDefinition().getLanguage() != Language.BPMN2) {
            return;
        }
        // достаём все шлюзы из нового определения БП
        Set<ParallelGateway> parallelGatewaysFromNewDefinition = getAllParallelGateways(processDefinitionUpdateData.getNewDefinition());
        if (parallelGatewaysFromNewDefinition.isEmpty()) {
            // если шлюзов нет - то зависания в них не должно произойти после обновления
            return;
        }
        // находим все параллельные шлюзы в новом определении БП, которые имеют несовместимости
        Set<NotCompatibleParallelGatewayInfo> notCompatibleParallelGateways = findNotCompatibleParallelGateways(parallelGatewaysFromNewDefinition,
                processDefinitionUpdateData.getNewDefinition(), processDefinitionUpdateData.getOldDefinition());
        if (notCompatibleParallelGateways.isEmpty()) {
            return;
        }
        if (processDefinitionUpdateData.inBatchMode()) {
            checkCompatibilityInBatchMode(processDefinitionUpdateData, notCompatibleParallelGateways);
        } else {
            checkCompatibility(processDefinitionUpdateData.getProcess().get(), notCompatibleParallelGateways);
        }
    }

    private Set<ParallelGateway> getAllParallelGateways(ParsedProcessDefinition definition) {
        Set<ParallelGateway> result = new HashSet<>();
        for (Node node : definition.getNodes(true)) {
            if (node instanceof ParallelGateway) {
                result.add((ParallelGateway) node);
            }
        }
        return result;
    }

    private Set<NotCompatibleParallelGatewayInfo> findNotCompatibleParallelGateways(Set<ParallelGateway> parallelGatewaysFromNewDefinition,
            ParsedProcessDefinition newDefinition, ParsedProcessDefinition oldDefinition) {
        Set<NotCompatibleParallelGatewayInfo> result = new HashSet<>();
        for (ParallelGateway parallelGateway : parallelGatewaysFromNewDefinition) {
            Node oldNode = oldDefinition.getNode(parallelGateway.getNodeId());
            String errorMessage = getParallelGatewayNotCompatibleErrorMessage(parallelGateway, oldNode);
            if (errorMessage == null) {
                continue;
            }

            Map<String, Set<Node>> transitionIdToPrecedessorNodes = new HashMap<String, Set<Node>>();
            Set<Node> allPrecedessorNodes = new HashSet<>();
            for (Transition transition : parallelGateway.getArrivingTransitions()) {
                Set<Node> precedessorNodes = new HashSet<>();
                getPrecedessorNodes(precedessorNodes, parallelGateway, transition.getFrom());
                transitionIdToPrecedessorNodes.put(transition.getNodeId(), precedessorNodes);
                allPrecedessorNodes.addAll(precedessorNodes);
            }
            // удаляем общие узлы, т.к. если выполнение находится в них - то они породят выполнение по всем переходам
            // может быть тут можно алгоритм побыстрее сделать...
            for (Node precedessorNode : allPrecedessorNodes) {
                boolean isCommonNode = true;
                for (Map.Entry<String, Set<Node>> entry : transitionIdToPrecedessorNodes.entrySet()) {
                    if (!entry.getValue().contains(precedessorNode)) {
                        isCommonNode = false;
                        break;
                    }
                }
                if (isCommonNode) {
                    for (Map.Entry<String, Set<Node>> entry : transitionIdToPrecedessorNodes.entrySet()) {
                        entry.getValue().remove(precedessorNode);
                    }
                }
            }
            result.add(new NotCompatibleParallelGatewayInfo(parallelGateway.getNodeId(), errorMessage, transitionIdToPrecedessorNodes));
        }
        return result;
    }

    private String getParallelGatewayNotCompatibleErrorMessage(ParallelGateway parallelGateway, Node oldNode) {
        if (oldNode == null) {
            return ProcessDefinitionNotCompatibleException.PARALLEL_GATEWAY_MISSED;
        }
        if (!(oldNode instanceof ParallelGateway)) {
            return ProcessDefinitionNotCompatibleException.PARALLEL_GATEWAY_MISTYPED;
        }
        if (!areInputTransitionsCompatible(parallelGateway, (ParallelGateway) oldNode)) {
            return ProcessDefinitionNotCompatibleException.PARALLEL_GATEWAY_TRANSITIONS;
        }
        return null;
    }

    private boolean areInputTransitionsCompatible(ParallelGateway newGateway, ParallelGateway oldGateway) {
        Set<String> newTransitionIds = newGateway.getArrivingTransitions().stream().map(t -> t.getNodeId()).collect(Collectors.toSet());
        Set<String> oldTransitionIds = oldGateway.getArrivingTransitions().stream().map(t -> t.getNodeId()).collect(Collectors.toSet());
        return newTransitionIds.containsAll(oldTransitionIds) && oldTransitionIds.containsAll(newTransitionIds);
    }

    private void getPrecedessorNodes(Set<Node> result, ParallelGateway breakOnCycledParallelGateway, Node node) {
        if (result.contains(node) || breakOnCycledParallelGateway.equals(node)) {
            return;
        }
        result.add(node);
        for (Transition transition : node.getArrivingTransitions()) {
            getPrecedessorNodes(result, breakOnCycledParallelGateway, transition.getFrom());
        }
    }

    private void checkCompatibilityInBatchMode(
            ProcessDefinitionUpdateData processDefinitionUpdateData,
            Set<NotCompatibleParallelGatewayInfo> notCompatibleParallelGateways
    ) {
        // по каждому несовместимому шлюзу отдельная проверка, их не ожидается много
        for (NotCompatibleParallelGatewayInfo notCompatibleParallelGatewayInfo : notCompatibleParallelGateways) {
            List<CurrentProcess> processes = currentTokenDao.findProcessesForParallelGatewayUpdateValidatorCheck(processDefinitionUpdateData.getOldDefinition()
                        .getId(), notCompatibleParallelGatewayInfo.nodeId, notCompatibleParallelGatewayInfo.allPrecedessorNodeIds);
            // сделать полностью запросом я это не смог, поэтому тут переиспользую штучную проверку
            for (CurrentProcess process : processes) {
                checkCompatibility(process, notCompatibleParallelGatewayInfo);
            }
        }
    }

    private void checkCompatibility(CurrentProcess process, Set<NotCompatibleParallelGatewayInfo> notCompatibleParallelGateways) {
        for (NotCompatibleParallelGatewayInfo notCompatibleParallelGatewayInfo : notCompatibleParallelGateways) {
            checkCompatibility(process, notCompatibleParallelGatewayInfo);
        }
    }

    private void checkCompatibility(CurrentProcess process, NotCompatibleParallelGatewayInfo notCompatibleParallelGatewayInfo) {
        List<String> activeNodeIds = currentTokenDao.findByProcessAndNodeIdsAndExecutionStatusIsNotEnded(process,
                notCompatibleParallelGatewayInfo.allPrecedessorNodeIds);
        List<CurrentToken> endedTokens = currentTokenDao.findByProcessAndNodeIdAndExecutionStatusIsEndedAndAbleToReactivateParent(process,
                notCompatibleParallelGatewayInfo.nodeId);
        if (activeNodeIds.isEmpty() && endedTokens.isEmpty()) {
            return;
        }
        // переходы, которые могут быть пройдены либо уже пройдены
        Set<String> activateableTransitionIds = new HashSet<>();
        Set<String> activatedTransitionIds = endedTokens.stream().map(t -> t.getTransitionId()).collect(Collectors.toSet());
        activateableTransitionIds.addAll(activatedTransitionIds);
        if (activatedTransitionIds.containsAll(notCompatibleParallelGatewayInfo.transitionIdToPrecedessorNodeIds.keySet())) {
            log.warn("Failed on " + process + ", frozen gateway " + notCompatibleParallelGatewayInfo.nodeId);
            throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.PARALLEL_GATEWAY_FROZEN,
                    new String[] { notCompatibleParallelGatewayInfo.nodeId });
        }
        for (String activeNodeId : activeNodeIds) {
            for (Map.Entry<String, Set<String>> entry : notCompatibleParallelGatewayInfo.transitionIdToPrecedessorNodeIds.entrySet()) {
                if (entry.getValue().contains(activeNodeId)) {
                    activateableTransitionIds.add(entry.getKey());
                }
            }
        }
        if (!activateableTransitionIds.containsAll(notCompatibleParallelGatewayInfo.transitionIdToPrecedessorNodeIds.keySet())) {
            log.warn("Failed on " + process + ", gateway " + notCompatibleParallelGatewayInfo.nodeId);
            log.warn("expectedTransitionIds: " + notCompatibleParallelGatewayInfo.transitionIdToPrecedessorNodeIds.keySet());
            log.warn("activateableTransitionIds: " + activateableTransitionIds);
            throw new ProcessDefinitionNotCompatibleException(notCompatibleParallelGatewayInfo.errorMessage,
                    new String[] { notCompatibleParallelGatewayInfo.nodeId });
        }
    }
}
