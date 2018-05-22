package ru.runa.wfe.task;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.lang.ActionEvent;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Executor;

public class TaskFactory {
    @Autowired
    private TaskDAO taskDAO;

    /**
     * creates a new task on the given task, in the given execution context.
     */
    public Task create(ExecutionContext executionContext, TaskDefinition taskDefinition, Swimlane swimlane, Executor executor, Integer index) {
        Task task = new Task(executionContext, taskDefinition);
        task.setIndex(index);
        taskDAO.create(task);
        taskDAO.flushPendingChanges();
        executionContext.addLog(new TaskCreateLog(task));
        taskDefinition.fireEvent(executionContext, ActionEvent.TASK_CREATE);
        task.setSwimlane(swimlane);
        task.assignExecutor(executionContext, executor != null ? executor : swimlane.getExecutor(), false);
        return task;
    }

}
