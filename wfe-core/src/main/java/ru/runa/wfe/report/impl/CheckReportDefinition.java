package ru.runa.wfe.report.impl;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import ru.runa.wfe.report.ReportConfigurationType.ReportConfigurationTypeVisitor;
import ru.runa.wfe.report.ReportParameterType;

import com.google.common.base.Strings;

/**
 * Checks report configuration definition
 */
public class CheckReportDefinition implements ReportConfigurationTypeVisitor<Boolean> {
    private final ReportAdminEditModel report;
    private final ReportAnalyzeResult analyzeResult;

    public CheckReportDefinition(ReportAdminEditModel report, ReportAnalyzeResult analyzeResult) {
        this.report = report;
        this.analyzeResult = analyzeResult;
    }

    @Override
    public Boolean onParameterBuilder() {
        if (Strings.isNullOrEmpty(report.getName())) {
            analyzeResult.appendMessage("Report name is not set");
        }
        if (!report.hasCompiledReport()) {
            analyzeResult.appendMessage("Report file is not set");
        }
        return Strings.isNullOrEmpty(analyzeResult.getMessage());
    }

    @Override
    public Boolean onRawSqlReport() {
        if (Strings.isNullOrEmpty(report.getName())) {
            analyzeResult.appendMessage("Report name is not set");
        }
        if (!report.hasCompiledReport()) {
            analyzeResult.appendMessage("Report file is not set");
        }
        JRParameter[] parameters;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(report.getCompiledReport());
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            parameters = jasperReport.getParameters();
        } catch (Exception e) {
            analyzeResult.appendMessage("Report file format is invalid");
            return false;
        }
        Set<String> reportParameters = new HashSet<String>();
        for (ReportAdminParameterEditModel param : report.getParameters()) {
            reportParameters.add(param.getInnerName());
            if (Strings.isNullOrEmpty(param.getName())) {
                analyzeResult.appendMessage("The name of parameter for user is not set");
            }
            if (Strings.isNullOrEmpty(param.getInnerName())) {
                analyzeResult.appendMessage("The name of the parameter in report is not set");
            }
        }
        for (JRParameter jrParam : parameters) {
            if (jrParam.isSystemDefined() || !jrParam.isForPrompting() || reportParameters.contains(jrParam.getName())) {
                continue;
            }
            ReportParameterType type = ReportParameterType.getForClass(jrParam.getValueClass());
            ReportAdminParameterEditModel editModel = new ReportAdminParameterEditModel(type, jrParam.getName(), jrParam.getDescription());
            analyzeResult.addRequiredParameter(editModel);
        }
        return Strings.isNullOrEmpty(analyzeResult.getMessage());
    }
}