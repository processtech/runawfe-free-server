package ru.runa.wfe.extension.handler;

import org.dom4j.Element;

public class ParamDef {
    private final String name;
    private String variableName;
    private Object value;
    private boolean optional;

    public ParamDef(String name, String variableName, boolean optional) {
        this.name = name;
        this.variableName = variableName;
        this.optional = optional;
    }

    public ParamDef(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public ParamDef(Element element) {
        name = element.attributeValue("name");
        variableName = element.attributeValue("variable");
        value = element.attributeValue("value");
        optional = Boolean.parseBoolean(element.attributeValue("optional", "false"));
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        if (value == null || value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer(name);
        if (variableName != null) {
            b.append(" [var=").append(variableName).append("]");
        }
        if (value != null) {
            b.append(" [value=").append(value).append("]");
        }
        return b.toString();
    }

}
