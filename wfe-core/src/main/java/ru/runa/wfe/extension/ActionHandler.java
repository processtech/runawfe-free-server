package ru.runa.wfe.extension;

import ru.runa.wfe.execution.ExecutionContext;

public interface ActionHandler extends Configurable {

    void execute(ExecutionContext executionContext) throws Exception;
}
