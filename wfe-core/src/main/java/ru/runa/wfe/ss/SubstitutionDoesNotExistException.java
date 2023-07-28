package ru.runa.wfe.ss;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that substitution does not exist.
 * 
 * Created on 03.02.2006
 */
public class SubstitutionDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public SubstitutionDoesNotExistException(String message) {
        super(message);
    }

}
