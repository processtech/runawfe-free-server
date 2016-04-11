package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;

public interface IExecutionContextFactory {

    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Token token);

    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Process process);

    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Task task);

}
