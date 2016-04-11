package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;

public class DefaultExecutionContextFactory implements IExecutionContextFactory {

    @Override
    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Token token) {
        return new ExecutionContext(processDefinition, token);
    }

    @Override
    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Process process) {
        return new ExecutionContext(processDefinition, process);
    }

    @Override
    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Task task) {
        return new ExecutionContext(processDefinition, task);
    }

}
