package ru.runa.wfe.var.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.CurrentVariable;

@Entity
@DiscriminatorValue(value = "N")
public class CurrentNullVariable extends CurrentVariable<Object> {

    @Override
    @Transient
    public Object getStorableValue() {
        return null;
    }

    @Override
    protected void setStorableValue(Object object) {
    }
}
