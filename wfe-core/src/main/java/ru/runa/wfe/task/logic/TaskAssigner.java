package ru.runa.wfe.task.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;

import com.google.common.base.Throwables;

public class TaskAssigner {
    private static final Log log = LogFactory.getLog(TaskAssigner.class);
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDAO taskDAO;

    @Transactional
    public void assignTask(Task task, boolean throwError) {
        if (task.getProcess().hasEnded()) {
            log.error("Deleting task for finished process: " + task);
            task.delete();
            return;
        }
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
            if (task.getSwimlane() != null) {
                Delegation delegation = processDefinition.getSwimlaneNotNull(task.getSwimlane().getName()).getDelegation();
                AssignmentHandler handler = delegation.getInstance();
                handler.assign(new ExecutionContext(processDefinition, task), task);
            }
            ProcessExecutionErrors.removeProcessError(task.getProcess().getId(), task.getNodeId());
        } catch (Throwable th) {
            log.warn("Unable to assign task '" + task + "' in " + task.getProcess() + " with swimlane '" + task.getSwimlane() + "'", th);
            if (throwError) {
                Throwables.propagate(th);
            } else {
                ProcessExecutionException e = new ProcessExecutionException(ProcessExecutionException.TASK_ASSIGNMENT_FAILED, th, task.getName());
                ProcessExecutionErrors.addProcessError(task, e);
            }
        }
    }

}
