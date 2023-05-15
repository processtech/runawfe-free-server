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
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
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
    private ProcessDao processDao;
    @Autowired
    private TokenDao tokenDao;
    @Autowired
    private MissingNodeProcessDefinitionUpdateValidator missingNodeProcessDefinitionUpdateValidator;
    @Autowired
    private ParallelGatewayProcessDefinitionUpdateValidator parallelGatewayProcessDefinitionUpdateValidator;

    public List<Process> findApplicableProcesses(ProcessDefinition oldDefinition) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        timeMeasurer.jobStarted();
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(oldDefinition.getName());
        filter.setDefinitionVersion(oldDefinition.getDeployment().getVersion());
        filter.setFinished(false);
        List<Process> processes = processDao.getProcesses(filter);
        timeMeasurer.jobEnded("Loading " + processes.size() + " active processes for " + oldDefinition);
        return processes;
    }

    public Set<Process> before(ProcessDefinition oldDefinition, ProcessDefinition newDefinition, List<Process> processes) {
        Set<Process> affectedProcesses;
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
            List<Process> processesForValidation = limit == -1 || limit >= processes.size() ? processes : processes.subList(0, limit);
            ProcessDefinitionUpdateData updateData = new ProcessDefinitionUpdateData(oldDefinition, newDefinition, processesForValidation);
            if (!SystemProperties.deleteTokensInMissingNodesOnDefinitionUpdate()) {
                missingNodeProcessDefinitionUpdateValidator.validate(updateData);
            }
            parallelGatewayProcessDefinitionUpdateValidator.validate(updateData);
            timeMeasurer.jobEnded("Validation of " + processesForValidation.size() + " processes");
        }
        return affectedProcesses;
    }

    public void after(ProcessDefinition newDefinition, Set<Process> processes) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        timeMeasurer.jobStarted();
        for (ru.runa.wfe.execution.Process process : processes) {
            Map<String, Set<String>> parallelGatewayTransitionsPassedByTokens = new HashMap<>();
            for (Token token : tokenDao.findByProcessAndNodeTypeAndAbleToReactivateParent(process, NodeType.PARALLEL_GATEWAY)) {
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
        for (ru.runa.wfe.execution.Process process : processes) {
            if (tokenDao.findByProcessAndExecutionStatusIsNotEnded(process).isEmpty()) {
                process.end(new ExecutionContext(newDefinition, process), null);
            }
        }
        timeMeasurer.jobEnded("Process activation");
    }

    private Set<Process> deleteTokensInMissingNodes(ProcessDefinition oldDefinition, ProcessDefinition newDefinition, List<Process> processes) {
        Set<Process> affectedProcesses = new HashSet<>();
        for (ru.runa.wfe.execution.Process process : processes) {
            for (Token token : tokenDao.findByProcessAndExecutionStatusIsNotEnded(process)) {
                if (newDefinition.getNode(token.getNodeId()) == null) {
                    token.end(oldDefinition, null, TaskCompletionInfo.createForHandler("incompatible definition update"), true);
                    affectedProcesses.add(process);
                }
            }
        }
        return affectedProcesses;
    }

}
