package ru.runa.wfe.execution.logic;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

public interface TaskExecutionListener {

    void afterTaskCreate(ExecutionContext executionContext, Task task);

    void beforeTaskDelete(ExecutionContext executionContext, Task task, TaskCompletionInfo completionInfo);
}
