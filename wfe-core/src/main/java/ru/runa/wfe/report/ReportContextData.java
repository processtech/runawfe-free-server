package ru.runa.wfe.report;

/**
 * Data that is required to build report via context menu.
 */
public class ReportContextData {
    private Long reportId;
    private String reportName;

    public ReportContextData(long reportId, String reportName) {
        this.reportId = reportId;
        this.reportName = reportName;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
}
