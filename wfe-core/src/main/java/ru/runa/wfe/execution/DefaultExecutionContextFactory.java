package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.task.Task;

public class DefaultExecutionContextFactory implements IExecutionContextFactory {

    @Override
    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token) {
        return new ExecutionContext(parsedProcessDefinition, token);
    }

    @Override
    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process) {
        return new ExecutionContext(parsedProcessDefinition, process);
    }

    @Override
    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Task task) {
        return new ExecutionContext(parsedProcessDefinition, task);
    }

}
