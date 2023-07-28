package ru.runa.wfe.extension.assign;

import ru.runa.wfe.LocalizableException;

/**
 * Indicates task assignment failure
 */
public class AssignmentException extends LocalizableException {
    private static final long serialVersionUID = 1L;

    public AssignmentException(String messageKey) {
        super(messageKey);
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
