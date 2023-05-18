package ru.runa.wfe.definition.update.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.definition.update.ProcessDefinitionUpdateData;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;

/**
 * Parallel gateway does not work in case of missed token in any of input transition. This validator prevents these errors.
 *
 * @author azyablin
 */
@Component
public class ParallelGatewayProcessDefinitionUpdateValidator implements ProcessDefinitionUpdateValidator {
    @Autowired
    TokenDao tokenDao;

    @Override
    public void validate(ProcessDefinitionUpdateData processDefinitionUpdateData) {
        if (processDefinitionUpdateData.getNewDefinition().getLanguage() != Language.BPMN2) {
            return;
        }
        Set<ParallelGateway> parallelGateways = getParallelGatewaysForCheck(processDefinitionUpdateData);
        for (ParallelGateway parallelGateway : parallelGateways) {
            Node newNode = processDefinitionUpdateData.getNewDefinition().getNode(parallelGateway.getNodeId());
            if (!(newNode instanceof ParallelGateway)) {
                throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.PARALLEL_GATEWAY_MISTYPED,
                        new String[] { parallelGateway.getNodeId() });
            }
            if (!areInputTransitionsCompatible(parallelGateway, (ParallelGateway) newNode)) {
                throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.PARALLEL_GATEWAY_TRANSITIONS,
                        new String[] { parallelGateway.getNodeId() });
            }
        }
    }

    /**
     * checks that nodes have not been added or updated in the new definition
     */
    private boolean areInputTransitionsCompatible(ParallelGateway oldNode, ParallelGateway newNode) {
        List<Transition> oldTransitions = oldNode.getArrivingTransitions();
        List<Transition> newTransitions = newNode.getArrivingTransitions();
        for (Transition newTransition : newTransitions) {
            if (!oldTransitions.stream().filter(new Predicate<Transition>() {
                @Override
                public boolean test(Transition t) {
                    return newTransition.getNodeId().equals(t.getNodeId());
                }
            }).findFirst().isPresent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Partial implementation
     * 
     * @return parallel gateways nearby to active nodes only
     */
    private Set<ParallelGateway> getParallelGatewaysForCheck(ProcessDefinitionUpdateData processDefinitionUpdateData) {
        Set<ParallelGateway> parallelGateways = new HashSet<>();
        Set<Node> seenNodes = new HashSet<>();
        for (CurrentProcess process : processDefinitionUpdateData.getProcesses()) {
            for (Token token : tokenDao.findByProcessAndExecutionStatusIsNotEnded(process)) {
                String nodeId = token.getNodeId();
                Node node = processDefinitionUpdateData.getOldDefinition().getNodeNotNull(nodeId);
                fetchNearestParallelGateways(parallelGateways, seenNodes, node);
            }
        }
        return parallelGateways;
    }

    private void fetchNearestParallelGateways(Set<ParallelGateway> parallelGateways, Set<Node> seenNodes, Node node) {
        if (seenNodes.contains(node)) {
            return;
        }
        seenNodes.add(node);
        if (node instanceof ParallelGateway) {
            if (node.getArrivingTransitions().size() > 1) {
                parallelGateways.add((ParallelGateway) node);
            }
            return;
        }
        for (Transition transition : node.getLeavingTransitions()) {
            fetchNearestParallelGateways(parallelGateways, seenNodes, transition.getTo());
        }
    }

}
