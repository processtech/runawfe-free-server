package ru.runa.wfe.var.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.var.ArchivedVariable;

@Entity
@DiscriminatorValue(value = "L")
public class ArchivedLongVariable extends ArchivedVariable<Long> {
    private Long object;

    @Override
    @Column(name = "LONGVALUE")
    public Long getStorableValue() {
        return object;
    }

    @Override
    protected void setStorableValue(Long object) {
        this.object = object;
    }

    @Override
    public boolean supports(Object value) {
        // ATTENTION! Same logic in CurrentLongVariable.
        return super.supports(value) || value instanceof Long;
    }
}
