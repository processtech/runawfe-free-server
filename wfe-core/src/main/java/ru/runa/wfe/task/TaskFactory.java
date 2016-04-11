package ru.runa.wfe.task;

import java.util.List;

import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.user.Executor;

public class TaskFactory {

    private String getDeadlineDuration(TaskDefinition taskDefinition) {
        if (taskDefinition.getDeadlineDuration() != null) {
            return taskDefinition.getDeadlineDuration();
        }
        List<CreateTimerAction> timerActions = taskDefinition.getNode().getTimerActions(true);
        if (timerActions.size() > 0) {
            return timerActions.get(0).getDueDate();
        }
        return SystemProperties.getDefaultTaskDeadline();
    }

    /**
     * creates a new task on the given task, in the given execution context.
     */
    public Task create(ExecutionContext executionContext, TaskDefinition taskDefinition, Swimlane swimlane, Executor executor, Integer index) {
        Process process = executionContext.getProcess();
        Task task = new Task(taskDefinition);
        Token token = executionContext.getToken();
        task.setToken(token);
        task.setProcess(process);
        task.setDeadlineDate(ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), getDeadlineDuration(taskDefinition)));
        task.setIndex(index);
        process.getTasks().add(task);
        ApplicationContextFactory.getTaskDAO().flushPendingChanges();
        executionContext.addLog(new TaskCreateLog(task));
        taskDefinition.fireEvent(executionContext, Event.TASK_CREATE);
        task.setSwimlane(swimlane);
        if (swimlane != null) {
            task.assignExecutor(executionContext, swimlane.getExecutor(), false);
        } else {
            task.assignExecutor(executionContext, executor, false);
        }
        return task;
    }

}
