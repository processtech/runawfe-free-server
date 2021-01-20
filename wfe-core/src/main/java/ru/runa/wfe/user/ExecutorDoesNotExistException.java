package ru.runa.wfe.user;

import ru.runa.wfe.LocalizableException;

/**
 * Signals that {@link Executor} does not exist in DB.
 */
public class ExecutorDoesNotExistException extends LocalizableException {
    private static final long serialVersionUID = -9096157439932169776L;

    private final String executorName;
    private final Class<? extends Executor> executorClass;

    public ExecutorDoesNotExistException(String executorName, Class<? extends Executor> executorClass) {
        super("error.executor.name.not.exist", executorName, executorClass);
        this.executorName = executorName;
        this.executorClass = executorClass;
    }

    public ExecutorDoesNotExistException(Long executorId, Class<? extends Executor> executorClass) {
        super("error.executor.id.not.exist", executorId, executorClass);
        this.executorName = executorId.toString();
        this.executorClass = executorClass;
    }

    public String getExecutorName() {
        return executorName;
    }

    public Class<? extends Executor> getExecutorClass() {
        return executorClass;
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
