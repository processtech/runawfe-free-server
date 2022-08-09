package ru.runa.wfe.script.report;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

@XmlType(name = DeployReportOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class DeployReportOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "deployReport";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String reportName;

    @XmlAttribute(name = AdminScriptConstants.DESCRIPTION_ATTRIBUTE_NAME)
    public String reportDescription;

    @XmlAttribute(name = AdminScriptConstants.TYPE_ATTRIBUTE_NAME)
    public String reportType;

    @XmlAttribute(name = AdminScriptConstants.FILE_ATTRIBUTE_NAME, required = true)
    public String reportFile;

    @XmlElement(name = "parameter", namespace = AdminScriptConstants.NAMESPACE)
    public List<XmlReportParameter> parameters = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, reportName);
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.FILE_ATTRIBUTE_NAME, reportFile);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        String category = Strings.isNullOrEmpty(reportType) ? "Script" : reportType;
        WfReport report = new WfReport(null, reportName, reportDescription, category, null);
        final Map<String, WfReportParameter> reportFileParameters = Maps.newHashMap();
        for (WfReportParameter p : context.getReportLogic().analyzeReportFile(report, context.getExternalResource(reportFile))) {
            reportFileParameters.put(p.getInternalName(), p);
        }

        List<WfReportParameter> reportParameters = Lists.transform(parameters, new Function<XmlReportParameter, WfReportParameter>() {
            int position = 0;

            @Override
            public WfReportParameter apply(XmlReportParameter input) {
                WfReportParameter parameter = reportFileParameters.get(input.innerName);
                if (parameter == null) {
                    throw new IllegalArgumentException("No parameter found by name " + input.innerName);
                }
                ReportParameterType type = ReportParameterType.valueOf(input.type);
                if (type == null) {
                    throw new IllegalArgumentException("No parameter type found by name " + input.type);
                }
                return new WfReportParameter(input.name, parameter.getDescription(), input.innerName, ++position, type, input.required);
            }
        });
        report = new WfReport(null, reportName, reportDescription, category, reportParameters);
        context.getReportLogic().deployReport(context.getUser(), report, context.getExternalResource(reportFile));
    }

    @Override
    public List<String> getExternalResources() {
        List<String> externalResources = super.getExternalResources();
        externalResources.add(reportFile);
        return externalResources;
    }
}
