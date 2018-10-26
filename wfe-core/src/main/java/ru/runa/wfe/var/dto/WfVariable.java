package ru.runa.wfe.var.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.StringFormat;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfVariable implements Serializable {
    private static final long serialVersionUID = 1L;

    private VariableDefinition definition;
    private Object value;

    public WfVariable() {
        definition = new VariableDefinition();
    }

    public WfVariable(String name, Object value) {
        Preconditions.checkNotNull(name);
        this.definition = new VariableDefinition(name, null, StringFormat.class.getName(), null);
        this.value = value;
    }

    public WfVariable(VariableDefinition definition, Object value) {
        Preconditions.checkNotNull(definition);
        this.definition = definition;
        this.value = value;
    }

    public VariableDefinition getDefinition() {
        return definition;
    }

    public Object getValue() {
        if (value != null) {
            return value;
        }
        return definition.getDefaultValue();
    }

    // TODO 2505 what reason for this?
    public Object getValueNoDefault() {
        return value;
    }

    public String getStringValue() {
        return definition.getFormatNotNull().format(value);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(definition);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfVariable) {
            return Objects.equal(definition.getName(), ((WfVariable) obj).definition.getName());
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("definition", definition).add("value", value).toString();
    }
}
