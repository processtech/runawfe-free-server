package ru.runa.wfe.relation;

import ru.runa.wfe.InternalApplicationException;

/**
 * Thrown if {@link Relation} not found.
 */
public class RelationDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public RelationDoesNotExistException(Object identity) {
        super(String.valueOf(identity));
    }

}
