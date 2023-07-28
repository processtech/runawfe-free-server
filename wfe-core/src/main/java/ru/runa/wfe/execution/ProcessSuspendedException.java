package ru.runa.wfe.execution;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that process suspended now and execution impossible.
 */
public class ProcessSuspendedException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public ProcessSuspendedException(Object identity) {
        super("Process suspened: " + identity);
    }

}
