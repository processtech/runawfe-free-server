package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * 
 * @author petr_mikheev
 */
public class BotDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = -9186710256485510506L;
    private final String botName;

    public BotDoesNotExistException(String botName) {
        super("Bot '" + botName + "' doesn't exist");
        this.botName = botName;
    }

    public String getBotName() {
        return botName;
    }
}
