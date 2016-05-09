package ru.runa.wfe.script.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = "IdentityType", namespace = AdminScriptConstants.NAMESPACE)
public class Identity {
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    public void validate(ScriptOperation scriptOperation) {
        ScriptValidation.requiredAttribute(scriptOperation, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
    }
}
