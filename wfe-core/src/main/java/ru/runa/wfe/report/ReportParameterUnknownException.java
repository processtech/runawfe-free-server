package ru.runa.wfe.report;

import ru.runa.wfe.InternalApplicationException;

public class ReportParameterUnknownException extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    private final String internalReportParameterName;

    public ReportParameterUnknownException(String internalReportParameterName) {
        super(internalReportParameterName + " is unknown");
        this.internalReportParameterName = internalReportParameterName;
    }

    public String getInternalReportParameterName() {
        return internalReportParameterName;
    }
}
