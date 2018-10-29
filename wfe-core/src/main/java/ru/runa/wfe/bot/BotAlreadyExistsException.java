package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * 
 * @author petr_mikheev
 */
public class BotAlreadyExistsException extends InternalApplicationException {
    private static final long serialVersionUID = -9186710256485510506L;
    private final String botName;

    public BotAlreadyExistsException(String botName) {
        super("Bot '" + botName + "' already exists.");
        this.botName = botName;
    }

    public String getBotName() {
        return botName;
    }
}
