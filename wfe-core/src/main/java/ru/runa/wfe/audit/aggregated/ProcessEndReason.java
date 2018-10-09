package ru.runa.wfe.audit.aggregated;

public enum ProcessEndReason {

    /**
     * Something wrong - end state has unsupported value.
     */
    UNKNOWN,

    /**
     * Process instance is not finished yet.
     */
    PROCESSING,

    /**
     * Process instance completed correct.
     */
    COMPLETED,

    /**
     * Process instance was cancelled.
     */
    CANCELLED
}
