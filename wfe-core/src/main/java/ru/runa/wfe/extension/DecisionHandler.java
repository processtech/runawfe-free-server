package ru.runa.wfe.extension;

import ru.runa.wfe.execution.ExecutionContext;

/**
 * decision handler interface for IF elements.
 */
public interface DecisionHandler extends Configurable {

    public String decide(ExecutionContext executionContext) throws Exception;

}
