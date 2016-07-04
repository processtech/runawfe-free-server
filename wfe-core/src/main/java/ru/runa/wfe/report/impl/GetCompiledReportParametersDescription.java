package ru.runa.wfe.report.impl;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import ru.runa.wfe.report.ReportConfigurationType.ReportConfigurationTypeVisitor;
import ru.runa.wfe.report.ReportFileIncorrectException;

/**
 * Gets parameters names from compiled report and puts them to parameters description.
 */
public class GetCompiledReportParametersDescription implements ReportConfigurationTypeVisitor<Map<String, String>> {

    private final byte[] compiledReport;

    public GetCompiledReportParametersDescription(byte[] compiledReport) {
        super();
        this.compiledReport = compiledReport;
    }

    @Override
    public Map<String, String> onParameterBuilder() {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> onRawSqlReport() {
        Map<String, String> parameters = new HashMap<String, String>();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(compiledReport);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            for (JRParameter jrParam : jasperReport.getParameters()) {
                if (jrParam.isSystemDefined() || !jrParam.isForPrompting()) {
                    continue;
                }
                parameters.put(jrParam.getName(), jrParam.getDescription());
            }
        } catch (Exception e) {
            throw new ReportFileIncorrectException(e);
        }
        return parameters;
    }
}
