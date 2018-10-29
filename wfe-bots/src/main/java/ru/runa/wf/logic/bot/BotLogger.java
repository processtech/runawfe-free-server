package ru.runa.wf.logic.bot;

import ru.runa.wfe.task.dto.WfTask;

public interface BotLogger {
    public void logError(WfTask task, Throwable th);

    public void logActivity();
}
