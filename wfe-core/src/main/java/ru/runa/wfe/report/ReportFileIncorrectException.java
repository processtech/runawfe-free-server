package ru.runa.wfe.report;

import ru.runa.wfe.InternalApplicationException;

public class ReportFileIncorrectException extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    public ReportFileIncorrectException() {
        super();
    }

    public ReportFileIncorrectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportFileIncorrectException(String message) {
        super(message);
    }

    public ReportFileIncorrectException(Throwable cause) {
        super(cause);
    }
}
