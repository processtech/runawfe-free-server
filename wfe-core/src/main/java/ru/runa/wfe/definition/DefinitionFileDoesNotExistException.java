package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that process definition archive doesn't contain file.
 */
public class DefinitionFileDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public DefinitionFileDoesNotExistException(String fileName) {
        super(fileName);
    }
}
