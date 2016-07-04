package ru.runa.wfe.report.impl;

import com.google.common.base.Strings;

/**
 * DTO with report build results.
 */
public class ReportBuildResult {

    private String message;

    /**
     * File name, if report generated as file and null, if report must be shown on HTML page.
     */
    private String reportFileName;

    /**
     * Report data: string with HTML to show on page or byte array if report generated as file.
     */
    private Object reportData;

    public ReportBuildResult(String reportFileName, Object reportData) {
        super();
        this.reportFileName = reportFileName;
        this.reportData = reportData;
    }

    public ReportBuildResult(Object reportData) {
        super();
        this.reportData = reportData;
    }

    public ReportBuildResult() {
        super();
    }

    public Object getReportData() {
        return reportData;
    }

    public void setReportData(Object reportData) {
        this.reportData = reportData;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void appendMessage(String message) {
        if (Strings.isNullOrEmpty(getMessage())) {
            setMessage(message);
        } else {
            setMessage(getMessage() + "; " + message);
        }
    }

    public void appendMessage(String parameterName, String message) {
        appendMessage("Parameter '" + parameterName + "': " + message);
    }
}
