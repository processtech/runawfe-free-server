package ru.runa.wfe.execution.logic;

import ru.runa.wfe.LocalizableException;

public class ProcessExecutionException extends LocalizableException {
    private static final long serialVersionUID = 1L;
    public static final String PARALLEL_GATEWAY_UNREACHABLE_TRANSITION = "error.parallel.gateway.unreachable.transition";

    public ProcessExecutionException(String message, Object... details) {
        super(message, details);
    }

    public ProcessExecutionException(String message, Throwable cause, Object... details) {
        super(message, cause, details);
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
