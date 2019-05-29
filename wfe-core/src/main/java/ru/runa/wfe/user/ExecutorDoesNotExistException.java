package ru.runa.wfe.user;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that {@link Executor} does not exist in DB.
 */
public class ExecutorDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = -9096157439932169776L;

    private final String executorName;
    private final Class<? extends Executor> executorClass;

    public ExecutorDoesNotExistException(String executorName, Class<? extends Executor> executorClass) {
        super("Executor " + executorName + " of class " + executorClass.getName() + " does not exist");
        this.executorName = executorName;
        this.executorClass = executorClass;
    }

    public ExecutorDoesNotExistException(Long executorId, Class<? extends Executor> executorClass) {
        this("with id = " + executorId, executorClass);
    }

    public String getExecutorName() {
        return executorName;
    }

    public Class<? extends Executor> getExecutorClass() {
        return executorClass;
    }
}
