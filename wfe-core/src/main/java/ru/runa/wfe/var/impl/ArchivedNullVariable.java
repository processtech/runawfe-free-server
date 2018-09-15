package ru.runa.wfe.var.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.ArchivedVariable;

@Entity
@DiscriminatorValue(value = "N")
public class ArchivedNullVariable extends ArchivedVariable<Object> {

    @Override
    @Transient
    public Object getStorableValue() {
        return null;
    }

    @Override
    protected void setStorableValue(Object object) {
    }
}
