package ru.runa.wfe.definition.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.definition.update.validator.MissingNodeProcessDefinitionUpdateValidator;
import ru.runa.wfe.definition.update.validator.ParallelGatewayProcessDefinitionUpdateValidator;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * @since rm362, rm2834
 */
@Component
public class ProcessDefinitionUpdateManager {
    private static final Log log = LogFactory.getLog(ProcessDefinitionUpdateManager.class);
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private CurrentTokenDao currentTokenDao;
    @Autowired
    private MissingNodeProcessDefinitionUpdateValidator missingNodeProcessDefinitionUpdateValidator;
    @Autowired
    private ParallelGatewayProcessDefinitionUpdateValidator parallelGatewayProcessDefinitionUpdateValidator;
    @Autowired
    private ExecutionLogic executionLogic;

    public List<CurrentProcess> findApplicableProcesses(ParsedProcessDefinition oldDefinition) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        timeMeasurer.jobStarted();
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(oldDefinition.getName());
        filter.setDefinitionVersion(oldDefinition.getVersion());
        filter.setFinished(false);
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        timeMeasurer.jobEnded("Loading " + processes.size() + " active processes for " + oldDefinition);
        return processes;
    }

    public Set<CurrentProcess> before(ParsedProcessDefinition oldDefinition, ParsedProcessDefinition newDefinition, List<CurrentProcess> processes) {
        Set<CurrentProcess> affectedProcesses;
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        if (SystemProperties.deleteTokensInMissingNodesOnDefinitionUpdate()) {
            timeMeasurer.jobStarted();
            affectedProcesses = deleteTokensInMissingNodes(oldDefinition, newDefinition, processes);
            timeMeasurer.jobEnded("Token deletion in missing nodes");
        } else {
            affectedProcesses = new HashSet<>();
        }
        if (SystemProperties.isDefinitionCompatibilityCheckEnabled()) {
            timeMeasurer.jobStarted();
            int limit = SystemProperties.getDefinitionCompatibilityCheckProcessesLimit();
            List<CurrentProcess> processesForValidation = limit == -1 || limit >= processes.size() ? processes : processes.subList(0, limit);
            ProcessDefinitionUpdateData updateData = new ProcessDefinitionUpdateData(oldDefinition, newDefinition, processesForValidation);
            if (!SystemProperties.deleteTokensInMissingNodesOnDefinitionUpdate()) {
                missingNodeProcessDefinitionUpdateValidator.validate(updateData);
            }
            parallelGatewayProcessDefinitionUpdateValidator.validate(updateData);
            timeMeasurer.jobEnded("Validation of " + processesForValidation.size() + " processes");
        }
        return affectedProcesses;
    }

    public void after(ParsedProcessDefinition newDefinition, Set<CurrentProcess> processes) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        timeMeasurer.jobStarted();
        for (CurrentProcess process : processes) {
            Map<String, Set<String>> parallelGatewayTransitionsPassedByTokens = new HashMap<>();
            for (Token token : currentTokenDao.findByProcessAndNodeTypeAndAbleToReactivateParent(process, NodeType.PARALLEL_GATEWAY)) {
                Set<String> transitionIds = parallelGatewayTransitionsPassedByTokens.get(token.getNodeId());
                if (transitionIds == null) {
                    transitionIds = new HashSet<>();
                    parallelGatewayTransitionsPassedByTokens.put(token.getNodeId(), transitionIds);
                }
                transitionIds.add(token.getTransitionId());
            }
            for (Map.Entry<String, Set<String>> entry : parallelGatewayTransitionsPassedByTokens.entrySet()) {
                ParallelGateway parallelGateway = (ParallelGateway) newDefinition.getNode(entry.getKey());
                if (parallelGateway == null) {
                    continue;
                }
                boolean allArrivingTransitionsArePassed = true;
                for (Transition transition : parallelGateway.getArrivingTransitions()) {
                    if (!entry.getValue().contains(transition.getNodeId())) {
                        allArrivingTransitionsArePassed = false;
                        break;
                    }
                }
                if (allArrivingTransitionsArePassed) {
                    parallelGateway.leave(new ExecutionContext(newDefinition, process));
                }
            }
        }
        timeMeasurer.jobEnded("Parallel gateway activation");
        timeMeasurer.jobStarted();
        for (CurrentProcess process : processes) {
            if (currentTokenDao.findByProcessAndExecutionStatusIsNotEnded(process).isEmpty()) {
                executionLogic.endProcess(process, new ExecutionContext(newDefinition, process), null);
            }
        }
        timeMeasurer.jobEnded("Process activation");
    }

    private Set<CurrentProcess> deleteTokensInMissingNodes(ParsedProcessDefinition oldDefinition, ParsedProcessDefinition newDefinition,
            List<CurrentProcess> processes) {
        Set<CurrentProcess> affectedProcesses = new HashSet<>();
        for (CurrentProcess process : processes) {
            for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatusIsNotEnded(process)) {
                if (newDefinition.getNode(token.getNodeId()) == null) {
                    executionLogic.endToken(token, oldDefinition, null, TaskCompletionInfo.createForHandler("incompatible definition update"), true);
                    affectedProcesses.add(process);
                }
            }
        }
        return affectedProcesses;
    }

}
