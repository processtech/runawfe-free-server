package ru.runa.wfe.extension;

import ru.runa.wfe.execution.ExecutionContext;

/**
 * Configured instances are executed on process end.
 * 
 * @author dofs
 * @since 4.3.0
 */
public interface ProcessEndHandler {

    public void execute(ExecutionContext executionContext) throws Exception;

}
