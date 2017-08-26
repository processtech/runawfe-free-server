package ru.runa.wfe.task.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.NoExecutorAssignedException;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;

public class TaskAssigner {
    private static final Log log = LogFactory.getLog(TaskAssigner.class);
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDAO taskDAO;

    @Transactional
    public boolean assignTask(Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        ProcessError processError = new ProcessError(ProcessErrorType.assignment, task.getProcess().getId(), task.getNodeId());
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
            if (task.getSwimlane() != null) {
                Delegation delegation = processDefinition.getSwimlaneNotNull(task.getSwimlane().getName()).getDelegation();
                AssignmentHandler handler = delegation.getInstance();
                handler.assign(new ExecutionContext(processDefinition, task), task);
            }
            if (task.getExecutor() != null) {
                Errors.removeProcessError(processError);
                return true;
            } else {
                Errors.addProcessError(processError, task.getName(), new NoExecutorAssignedException());
            }
        } catch (Throwable th) {
            if (Errors.addProcessError(processError, task.getName(), th)) {
                log.warn("Unable to assign task '" + task + "' in " + task.getProcess() + " with swimlane '" + task.getSwimlane() + "'", th);
            }
        }
        return false;
    }

}
