package ru.runa.wfe.task.logic;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.extension.assign.NoExecutorAssignedException;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.task.Task;

@CommonsLog
public class TaskAssigner {
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ExecutionLogic executionLogic;

    public boolean assignTask(Task task) {
        try {
            ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(task.getProcess());
            if (task.getSwimlane() != null) {
                Delegation delegation = parsedProcessDefinition.getSwimlaneNotNull(task.getSwimlane().getName()).getDelegation();
                AssignmentHandler handler = delegation.getInstance();
                handler.assign(new ExecutionContext(parsedProcessDefinition, task), task);
            }
            if (task.getExecutor() != null) {
                executionLogic.removeTokenError(task.getToken());
                return true;
            } else {
                if (task.getToken().getExecutionStatus() != ExecutionStatus.FAILED) {
                    executionLogic.failToken(task.getToken(), new NoExecutorAssignedException());
                }
            }
        } catch (Throwable th) {
            if (executionLogic.failToken(task.getToken(), th)) {
                if (th instanceof AssignmentException) {
                    log.warn("Unable to assign task '" + task + "' in " + task.getProcess() + " with swimlane '" + task.getSwimlane() + "': "
                            + th.getMessage());
                } else {
                    log.warn("Unable to assign task '" + task + "' in " + task.getProcess() + " with swimlane '" + task.getSwimlane() + "'", th);
                }
            }
        }
        return false;
    }
}
