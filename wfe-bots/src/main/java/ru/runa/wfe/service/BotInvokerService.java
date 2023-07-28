package ru.runa.wfe.service;

import javax.ejb.Remote;
import ru.runa.wfe.bot.BotStation;

@Remote
public interface BotInvokerService {

    /**
     * Execute pending tasks in bot station once.
     */
    void invokeBots(BotStation botStation);

    /**
     * Start bot station (tasks are executed periodically).
     */
    void startPeriodicBotsInvocation(BotStation botStation);

    /**
     * Tests whether bot station is started.
     */
    boolean isRunning();

    /**
     * Stop bot station (if it was started).
     */
    void cancelPeriodicBotsInvocation();
}
