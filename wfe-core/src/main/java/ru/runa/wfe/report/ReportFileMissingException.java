package ru.runa.wfe.report;

public class ReportFileMissingException extends ReportException {
    private static final long serialVersionUID = 1L;

    public ReportFileMissingException() {
        super("error.report.missing.file");
    }
}
