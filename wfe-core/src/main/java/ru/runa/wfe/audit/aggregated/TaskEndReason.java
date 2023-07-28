package ru.runa.wfe.audit.aggregated;

public enum TaskEndReason {

    /**
     * Something wrong - unsupported value e.t.c.
     */
    UNKNOWN,

    /**
     * Task is in processing.
     */
    PROCESSING,

    /**
     * Task is completed normally.
     */
    COMPLETED,

    /**
     * Task was cancelled.
     */
    CANCELLED,

    /**
     * Task was timeout.
     */
    TIMEOUT,

    /**
     * Task was end by substitutor.
     */
    SUBSTITUTOR_END,

    /**
     * Task was end by process end.
     */
    PROCESS_END,

    /**
     * Task was end by admin.
     */
    ADMIN_END
}
