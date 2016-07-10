package ru.runa.wfe.execution;

/**
 * Process and token execution statuses.
 *
 * @author Alex Chernyshev
 */
public enum ExecutionStatus {
    ACTIVE,
    SUSPENDED,
    FAILED,
    ENDED;

    public String getLabelKey() {
        return "process.execution.status." + name().toLowerCase();
    }
}