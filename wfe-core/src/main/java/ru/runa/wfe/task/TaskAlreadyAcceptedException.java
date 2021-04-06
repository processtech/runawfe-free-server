package ru.runa.wfe.task;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signal that task was already accepted by another user (during assignment on
 * multiple actors).
 */
public class TaskAlreadyAcceptedException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public TaskAlreadyAcceptedException(String taskName) {
        super(taskName);
    }

}
