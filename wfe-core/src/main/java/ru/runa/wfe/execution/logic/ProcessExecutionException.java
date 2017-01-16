package ru.runa.wfe.execution.logic;

import ru.runa.wfe.LocalizableException;

public class ProcessExecutionException extends LocalizableException {
    private static final long serialVersionUID = 1L;
    public static final String TASK_ASSIGNMENT_FAILED = "error.task.assignment";
    public static final String SWIMLANE_ASSIGNMENT_FAILED = "error.swimlane.assignment";
    public static final String BOT_TASK_CONFIGURATION_ERROR = "error.bottask.configuration";
    public static final String BOT_TASK_MISSED = "error.bottask.missed";
    public static final String TIMER_EXECUTION_FAILED = "error.timer.execution";
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
