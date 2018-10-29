package ru.runa.wfe.bot.invoker;

import ru.runa.wfe.bot.BotStation;

/**
 * Invoke bots on this server.
 *
 * @author dofs
 * @since 2.0
 */
public interface BotInvoker {

    public void invokeBots(BotStation botStation, boolean resetFailedDelay);

}
