package ru.runa.wfe.var.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.ArchivedVariable;

@Entity
@DiscriminatorValue(value = "S")
public class ArchivedStringVariable extends ArchivedVariable<String> {

    @Override
    @Transient
    public String getStorableValue() {
        return getStringValue();
    }

    @Override
    protected void setStorableValue(String object) {
    }

    @Override
    public boolean supports(Object value) {
        // ATTENTION! Same logic in CurrentStringVariable.
        return super.supports(value) || value instanceof String && ((String) value).length() <= getMaxStringSize();
    }
}
