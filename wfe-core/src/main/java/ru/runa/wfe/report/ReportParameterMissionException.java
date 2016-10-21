package ru.runa.wfe.report;

import ru.runa.wfe.InternalApplicationException;

public class ReportParameterMissionException extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    private final String internalReportParameterName;

    public ReportParameterMissionException(String internalReportParameterName) {
        super(internalReportParameterName + " is missing");
        this.internalReportParameterName = internalReportParameterName;
    }

    public String getInternalReportParameterName() {
        return internalReportParameterName;
    }

}
