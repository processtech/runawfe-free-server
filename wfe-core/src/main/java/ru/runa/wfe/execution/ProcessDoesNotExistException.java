package ru.runa.wfe.execution;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that process does not exist in system.
 */
public class ProcessDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public ProcessDoesNotExistException(Object identity) {
        super("Process does not exist: " + identity);
    }

}
