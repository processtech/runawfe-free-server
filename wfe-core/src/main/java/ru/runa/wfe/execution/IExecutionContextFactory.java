package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.task.Task;

public interface IExecutionContextFactory {

    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token);

    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process);

    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Task task);

}
