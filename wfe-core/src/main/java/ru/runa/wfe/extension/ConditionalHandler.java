package ru.runa.wfe.extension;

import ru.runa.wfe.execution.ExecutionContext;

public interface ConditionalHandler extends Configurable {
    boolean evaluate(ExecutionContext context) throws Exception;
}
