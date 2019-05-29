package ru.runa.wfe.var.impl;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.VariableDefinition;

@Entity
@DiscriminatorValue(value = "D")
public class CurrentDateVariable extends CurrentVariable<Date> {
    private Date object;

    @Override
    @Column(name = "DATEVALUE")
    public Date getStorableValue() {
        return object;
    }

    @Override
    protected void setStorableValue(Date object) {
        this.object = object;
    }

    @Override
    public boolean supports(Object value) {
        // ATTENTION! Same logic in ArchivedDateVariable.
        return super.supports(value) || value instanceof Date;
    }

    @Override
    public String toString(Object value, VariableDefinition variableDefinition) {
        // ATTENTION! Same logic in ArchivedDateVariable.
        return variableDefinition.getFormatNotNull().format(value);
    }
}
