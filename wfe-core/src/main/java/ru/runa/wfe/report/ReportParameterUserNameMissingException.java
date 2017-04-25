package ru.runa.wfe.report;

public class ReportParameterUserNameMissingException extends ReportException {

    private static final long serialVersionUID = 1L;

    public ReportParameterUserNameMissingException(String internalReportParameterName) {
        super("error.report.parameter.visible.name", internalReportParameterName);
    }
}
