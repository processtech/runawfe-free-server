package ru.runa.wfe.relation;

import ru.runa.wfe.InternalApplicationException;

/**
 * Thrown if {@link RelationPair} does not exist.
 */
public class RelationPairDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public RelationPairDoesNotExistException(Object identity) {
        super(String.valueOf(identity));
    }

}
