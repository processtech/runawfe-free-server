package ru.runa.wfe.execution.dto;

public enum RestoreProcessStatus {
    OK,
    ONLY_ASYNC_SUBPROCESS_CAN_BE_RESTORED,
    PROCESS_HAS_BEEN_COMPLETED,
    /**
     * Actual for old processes only (ProcessCancelLog was generated in middle of Process.end(..)).
     * 
     * We are unable to restore such processes.
     */
    UNABLE_TO_FIND_ACTIVE_TOKENS_BY_PROCESS_END_DATE,
}
