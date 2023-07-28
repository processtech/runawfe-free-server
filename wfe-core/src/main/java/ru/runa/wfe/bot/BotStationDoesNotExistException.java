package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * 
 * @author petr_mikheev
 */
public class BotStationDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    private final String botstationName;

    public BotStationDoesNotExistException(String botStationName) {
        super("BotStation '" + botStationName + "' does not exist");
        botstationName = botStationName;
    }

    public String getBotStationName() {
        return botstationName;
    }
}
