package ru.runa.wfe.report;

import ru.runa.wfe.LocalizableException;

public abstract class ReportException extends LocalizableException {

    private static final long serialVersionUID = 1L;

    public ReportException(String messageKey, Object... details) {
        super(messageKey, details);
    }

    public ReportException(String messageKey, Throwable cause, Object... details) {
        super(messageKey, cause, details);
    }

    @Override
    protected String getResourceBaseName() {
        return "report.error";
    }
}
