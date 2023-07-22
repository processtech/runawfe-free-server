package ru.runa.wfe.task;

public enum TaskCompletionBy {
    TIMER,
    ASSIGNED_EXECUTOR,
    SUBSTITUTOR,
    ADMIN,
    HANDLER,
    SIGNAL,
    PROCESS_END,
    EMBEDDED_SUBPROCESS_END
}
