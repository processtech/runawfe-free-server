package ru.runa.wfe.var.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.CurrentVariable;

@Entity
@DiscriminatorValue(value = "S")
public class CurrentStringVariable extends CurrentVariable<String> {

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
        // ATTENTION! Same logic in ArchivedStringVariable.
        return super.supports(value) || value instanceof String && ((String) value).length() <= getMaxStringSize();
    }
}
