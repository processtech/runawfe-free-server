package ru.runa.wfe.script.report;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

@XmlType(name = "ReportParameterType", namespace = AdminScriptConstants.NAMESPACE)
public class XmlReportParameter {

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.TYPE_ATTRIBUTE_NAME, required = true)
    public String type;

    @XmlAttribute(name = "innerName", required = true)
    public String innerName;

    @XmlAttribute(name = "required")
    public boolean required;

    public void validate(ScriptExecutionContext context, ScriptOperation operation) {
        ScriptValidation.requiredAttribute(operation, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        ScriptValidation.requiredAttribute(operation, "innerName", innerName);
    }

}
