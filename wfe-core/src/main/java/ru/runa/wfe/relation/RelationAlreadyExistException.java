package ru.runa.wfe.relation;

import ru.runa.wfe.InternalApplicationException;

/**
 * Exception, which was thrown, if {@link Relation} already found.
 */
public class RelationAlreadyExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    /**
     * Create instance with specified relation name.
     */
    public RelationAlreadyExistException(String relationName) {
        super(relationName);
    }
}
