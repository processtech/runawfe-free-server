package ru.runa.wfe.report;

public class ReportNameMissingException extends ReportException {

    private static final long serialVersionUID = 1L;

    public ReportNameMissingException() {
        super("error.report.missing.name");
    }
}
