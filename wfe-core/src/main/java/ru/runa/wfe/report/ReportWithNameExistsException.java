package ru.runa.wfe.report;

public class ReportWithNameExistsException extends ReportException {

    private static final long serialVersionUID = 1L;

    public ReportWithNameExistsException(String reportName) {
        super("error.report.already.exists", reportName);
    }
}
