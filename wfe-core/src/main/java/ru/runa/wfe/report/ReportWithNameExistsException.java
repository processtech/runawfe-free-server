package ru.runa.wfe.report;

import ru.runa.wfe.InternalApplicationException;

public class ReportWithNameExistsException extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    private String reportName;

    public ReportWithNameExistsException() {
        super();
    }

    public ReportWithNameExistsException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public ReportWithNameExistsException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ReportWithNameExistsException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

}
