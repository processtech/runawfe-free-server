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
 * Выполняет проверку корректности конфигурации отчета.
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
            analyzeResult.appendMessage("Не задано название отчета");
        }
        if (!report.hasCompiledReport()) {
            analyzeResult.appendMessage("Не задан файл отчёта");
        }
        return Strings.isNullOrEmpty(analyzeResult.getMessage());
    }

    @Override
    public Boolean onRawSqlReport() {
        if (Strings.isNullOrEmpty(report.getName())) {
            analyzeResult.appendMessage("Не задано название отчета");
        }
        if (!report.hasCompiledReport()) {
            analyzeResult.appendMessage("Не задан файл отчёта");
        }
        JRParameter[] parameters;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(report.getCompiledReport());
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            parameters = jasperReport.getParameters();
        } catch (Exception e) {
            analyzeResult.appendMessage("Формат файла отчета не распознан");
            return false;
        }
        Set<String> reportParameters = new HashSet<String>();
        for (ReportAdminParameterEditModel param : report.getParameters()) {
            reportParameters.add(param.getInnerName());
            if (Strings.isNullOrEmpty(param.getName())) {
                analyzeResult.appendMessage("Не задано название параметра для пользователя");
            }
            if (Strings.isNullOrEmpty(param.getInnerName())) {
                analyzeResult.appendMessage("Не задано название параметра в отчёте");
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