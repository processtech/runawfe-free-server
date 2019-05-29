package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * 
 * @author petr_mikheev
 */
public class BotTaskAlreadyExistsException extends InternalApplicationException {
    private static final long serialVersionUID = -9186710256485510506L;
    private final String botTaskName;

    public BotTaskAlreadyExistsException(String botTaskName) {
        super("BotTask '" + botTaskName + "' already exists.");
        this.botTaskName = botTaskName;
    }

    public String getBotTaskName() {
        return botTaskName;
    }
}
