package ru.runa.wfe.var.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.var.Variable;

@Entity
@DiscriminatorValue(value = "N")
public class NullVariable extends Variable<Object> {

    @Override
    @Transient
    public Object getStorableValue() {
        return null;
    }

    @Override
    protected void setStorableValue(Object object) {

    }

}
