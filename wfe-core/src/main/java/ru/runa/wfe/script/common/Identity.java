package ru.runa.wfe.script.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import com.google.common.base.Strings;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = "IdentityType", namespace = AdminScriptConstants.NAMESPACE)
public class Identity {
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.ID_ATTRIBUTE_NAME)
    public String id;

    public void validate(ScriptOperation scriptOperation) {
        if (Strings.isNullOrEmpty(name) == Strings.isNullOrEmpty(id)) {
            throw new ScriptValidationException("Either attributes 'name' or 'id' must be present in identity attribute, but not both");
        }
        if (!Strings.isNullOrEmpty(name)) {
            ScriptValidation.requiredAttribute(scriptOperation, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        }
        if (!Strings.isNullOrEmpty(id)) {
            ScriptValidation.requiredAttribute(scriptOperation, AdminScriptConstants.ID_ATTRIBUTE_NAME, id);
        }
    }
}
