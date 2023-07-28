package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that deployed definition already exists
 * 
 */
public class DefinitionAlreadyExistException extends InternalApplicationException {

    private static final long serialVersionUID = 6474246489536171787L;
    private final String name;

    public DefinitionAlreadyExistException(String name) {
        super("Definition " + name + " already exists");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
