package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "R")
public class ArchivedVariableCreateLog extends ArchivedVariableLog implements VariableCreateLog {

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE_CREATE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getVariableNameNotNull(), getVariableNewValueForPattern() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableCreateLog(this);
    }
}
