package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that process definition does not exist in DB.
 */
public class DefinitionDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    private final String quotedName;

    public DefinitionDoesNotExistException(String name) {
        super("Definition \"" + name + "\" does not exist");
        this.quotedName = "\"" + name + "\"";
    }

    public String getQuotedName() {
        return quotedName;
    }
}
