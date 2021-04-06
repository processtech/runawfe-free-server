package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * @author petr_mikheev
 */
public class BotStationAlreadyExistsException extends InternalApplicationException {
    private static final long serialVersionUID = -9186710256485510506L;
    private final String botstationName;

    public BotStationAlreadyExistsException(String botstationName) {
        super("BotStation '" + botstationName + "' already exists.");
        this.botstationName = botstationName;
    }

    public String getBotStationName() {
        return botstationName;
    }
}
