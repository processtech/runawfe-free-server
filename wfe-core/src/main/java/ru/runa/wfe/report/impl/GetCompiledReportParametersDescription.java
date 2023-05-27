package ru.runa.wfe.report.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import ru.runa.wfe.report.ReportConfigurationType.ReportConfigurationTypeVisitor;
import ru.runa.wfe.report.ReportFileIncorrectException;
import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.WfReportParameter;

/**
 * Gets parameters names from compiled report and puts them to parameters description.
 */
public class GetCompiledReportParametersDescription implements ReportConfigurationTypeVisitor<List<WfReportParameter>> {

    private final byte[] compiledReport;

    public GetCompiledReportParametersDescription(byte[] compiledReport) {
        this.compiledReport = compiledReport;
    }

    @Override
    public List<WfReportParameter> onParameterBuilder() {
        return new ArrayList<>();
    }

    @Override
    public List<WfReportParameter> onRawSqlReport() {
        List<WfReportParameter> parameters = new ArrayList<>();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(compiledReport);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            for (JRParameter jrParam : jasperReport.getParameters()) {
                if (jrParam.isSystemDefined() || !jrParam.isForPrompting()) {
                    continue;
                }
                WfReportParameter parameter = new WfReportParameter();
                parameter.setUserName(jrParam.getName());
                parameter.setInternalName(jrParam.getName());
                parameter.setDescription(jrParam.getDescription());
                parameter.setType(ReportParameterType.getForClass(jrParam.getValueClass()));
                parameters.add(parameter);
            }
        } catch (Throwable e) {
            throw new ReportFileIncorrectException(e);
        }
        return parameters;
    }
}
