package ru.runa.wfe.user;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that {@link Executor} already exists.
 * 
 * @since 2.0
 */
public class ExecutorAlreadyExistsException extends InternalApplicationException {
    private static final long serialVersionUID = -1082771372061746496L;
    private final String executorName;

    public ExecutorAlreadyExistsException(String executorName) {
        super("Executor " + executorName + " already exists.");
        this.executorName = executorName;
    }

    public String getExecutorName() {
        return executorName;
    }

    public ExecutorAlreadyExistsException(Long code) {
        this("with code " + code);
    }
}
