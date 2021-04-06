package ru.runa.wfe.presentation.filter;

import ru.runa.wfe.InternalApplicationException;

/**
 * Indicates about invalid format in {@link FilterCriteria} subclasses.
 */
public class FilterFormatException extends InternalApplicationException {

    private static final long serialVersionUID = 8250242532336258812L;

    public FilterFormatException(String message) {
        super(message);
    }
}
