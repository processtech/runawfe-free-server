package ru.runa.wfe.extension.assign;

import ru.runa.wfe.LocalizableException;

public class NoExecutorAssignedException extends LocalizableException {
    private static final long serialVersionUID = 1L;

    public NoExecutorAssignedException() {
        super("error.assignment.executors.empty");
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
