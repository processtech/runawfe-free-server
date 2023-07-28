package ru.runa.wfe.task;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that task does not exist in DB.
 */
public class TaskDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public TaskDoesNotExistException(Object identity) {
        super("Task does not exist: " + identity);
    }

}
