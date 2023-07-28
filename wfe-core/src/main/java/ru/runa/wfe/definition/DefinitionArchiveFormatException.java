package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that process definition file is not archive or archive has
 * unsupported format.
 */
public class DefinitionArchiveFormatException extends InternalApplicationException {

    private static final long serialVersionUID = 691334070620809955L;

    public DefinitionArchiveFormatException(Throwable cause) {
        super(cause);
    }
}
