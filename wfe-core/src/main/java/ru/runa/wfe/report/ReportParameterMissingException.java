package ru.runa.wfe.report;

public class ReportParameterMissingException extends ReportException {

    private static final long serialVersionUID = 1L;

    public ReportParameterMissingException(String internalReportParameterName) {
        super("error.report.missing.parameter", internalReportParameterName);
    }
}
