package ru.runa.wfe.service;

import javax.ejb.Remote;

import ru.runa.wfe.bot.BotStation;

@Remote
public interface BotInvokerService {

    /**
     * Execute pending tasks in bot station once.
     * 
     * @param botStation
     */
    public void invokeBots(BotStation botStation);

    /**
     * Start bot station (tasks are executed periodically).
     * 
     * @param botStation
     */
    public void startPeriodicBotsInvocation(BotStation botStation);

    /**
     * Tests whether bot station is started.
     * 
     * @return
     */
    public boolean isRunning();

    /**
     * Stop bot station (if it was started).
     */
    public void cancelPeriodicBotsInvocation();

}
