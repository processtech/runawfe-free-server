package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = "PermissionType", namespace = AdminScriptConstants.NAMESPACE)
public class Permission {
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

}
