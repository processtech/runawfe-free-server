package ru.runa.wfe.var.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.var.CurrentVariable;

@Entity
@DiscriminatorValue(value = "O")
public class CurrentDoubleVariable extends CurrentVariable<Double> {
    private Double object;

    @Override
    @Column(name = "DOUBLEVALUE")
    public Double getStorableValue() {
        return object;
    }

    @Override
    protected void setStorableValue(Double object) {
        this.object = object;
    }

    @Override
    public boolean supports(Object value) {
        // ATTENTION! Same logic in ArchivedDoubleVariable.
        return super.supports(value) || value instanceof Double;
    }
}
