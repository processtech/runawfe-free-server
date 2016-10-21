package ru.runa.wfe.script.substitution;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = "SubstitutionType", namespace = AdminScriptConstants.NAMESPACE)
public class XmlSubstitution {

    @XmlAttribute(name = "orgFunc", required = true)
    public String orgFunction;

    @XmlAttribute(name = "criteria", required = false)
    public Long criteriaId;

    @XmlAttribute(name = "isEnabled", required = false)
    public Boolean isEnabled = true;

    @XmlAttribute(name = "isFirst", required = false)
    public Boolean isFirst = true;

}
