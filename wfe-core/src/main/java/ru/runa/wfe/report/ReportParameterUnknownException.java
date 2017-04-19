package ru.runa.wfe.report;

public class ReportParameterUnknownException extends ReportException {

    private static final long serialVersionUID = 1L;

    public ReportParameterUnknownException(String internalReportParameterName) {
        super("error.report.parameter.unknown", internalReportParameterName);
    }
}
