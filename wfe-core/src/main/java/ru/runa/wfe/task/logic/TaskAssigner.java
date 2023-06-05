package ru.runa.wfe.task.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.extension.assign.NoExecutorAssignedException;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;

public class TaskAssigner {
    private static final Log log = LogFactory.getLog(TaskAssigner.class);
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    public boolean assignTask(Task task) {
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
            if (task.getSwimlane() != null) {
                Delegation delegation = processDefinition.getSwimlaneNotNull(task.getSwimlane().getName()).getDelegation();
                AssignmentHandler handler = delegation.getInstance();
                handler.assign(new ExecutionContext(processDefinition, task), task);
            }
            if (task.getExecutor() != null) {
                task.getToken().removeError();
                return true;
            } else {
                if (task.getToken().getExecutionStatus() != ExecutionStatus.FAILED) {
                    task.getToken().fail(new NoExecutorAssignedException());
                }
            }
        } catch (Throwable th) {
            if (task.getToken().fail(th)) {
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
