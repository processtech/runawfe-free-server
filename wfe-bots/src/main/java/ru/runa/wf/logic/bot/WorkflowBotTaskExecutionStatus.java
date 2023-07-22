package ru.runa.wf.logic.bot;

/**
 * Current status of bot execution component.
 */
public enum WorkflowBotTaskExecutionStatus {
    /**
     * Bot execution was scheduled. Task handler will be called soon.
     */
    SCHEDULED,

    /**
     * Bot execution started. Task handler executed right now.
     */
    STARTED,

    /**
     * Bot execution completed success. Task handler already do it's work.
     */
    COMPLETED,

    /**
     * Failed to execute. Task handler throws exception or stuck.
     */
    FAILED,

    /**
     * Bot execution was scheduled, but task handler will not be called. It may
     * be in case of sequential task or bot execution. Rescheduling is required
     * for such tasks.
     */
    SCHEDULING_FAILURE
}
