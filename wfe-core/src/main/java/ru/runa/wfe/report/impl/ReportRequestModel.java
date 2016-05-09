package ru.runa.wfe.report.impl;

import java.util.ArrayList;

/**
 * Модель для запроса параметров построения отчета.
 */
public class ReportRequestModel {

    /**
     * Идентификатор отчета.
     */
    private Long reportId;

    /**
     * Название отчёта.
     */
    private String reportName;

    /**
     * Параметры, которые требуются для построения отчета.
     */
    private ArrayList<ReportParameterModel> reportParameters;

    /**
     * Тип создаваемого отчёта.
     */
    private ReportGenerationType generationType = ReportGenerationType.HTML_EMBEDDED;

    public ReportRequestModel() {
    }

    public ReportRequestModel(Long reportId, String reportName, ArrayList<ReportParameterModel> reportParameters) {
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportParameters = reportParameters;
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

    public ArrayList<ReportParameterModel> getReportParameters() {
        return reportParameters;
    }

    public void setReportParameters(ArrayList<ReportParameterModel> reportParameters) {
        this.reportParameters = reportParameters;
    }

    public ReportGenerationType getGenerationType() {
        return generationType;
    }

    public void setGenerationType(ReportGenerationType generationType) {
        this.generationType = generationType;
    }
}
