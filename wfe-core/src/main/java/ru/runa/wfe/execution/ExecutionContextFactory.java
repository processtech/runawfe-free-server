package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.task.Task;

public class ExecutionContextFactory {

    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token) {
        return new ExecutionContext(parsedProcessDefinition, token);
    }

    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process) {
        return new ExecutionContext(parsedProcessDefinition, process);
    }

    public ExecutionContext createExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Task task) {
        return new ExecutionContext(parsedProcessDefinition, task);
    }
}
