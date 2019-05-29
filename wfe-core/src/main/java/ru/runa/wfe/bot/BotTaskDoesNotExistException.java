package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * 
 * @author petr_mikheev
 */
public class BotTaskDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = -9186710256485510506L;
    private final String botTaskName;

    public BotTaskDoesNotExistException(String botTaskName) {
        super("BotTask '" + botTaskName + "' does not exist");
        this.botTaskName = botTaskName;
    }

    public String getBotTaskName() {
        return botTaskName;
    }
}
