package ru.runa.wfe.bot.invoker;

import ru.runa.wf.logic.bot.BotStationResources;

/**
 * Created on 23.03.2005
 *
 */
public class BotInvokerFactory {
    private static BotInvoker instance = null;

    public static synchronized BotInvoker getBotInvoker() {
        if (instance == null) {
            instance = BotStationResources.createBotInvoker();
        }
        return instance;
    }

}
