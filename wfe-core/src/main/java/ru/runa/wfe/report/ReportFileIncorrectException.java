package ru.runa.wfe.report;

public class ReportFileIncorrectException extends ReportException {

    private static final long serialVersionUID = 1L;

    public ReportFileIncorrectException(Throwable cause) {
        super("error.report.incorrect.file");
    }
}
