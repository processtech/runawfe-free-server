package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "W")
public class ArchivedVariableUpdateLog extends ArchivedVariableLog implements VariableUpdateLog {

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE_UPDATE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getVariableName(), getVariableNewValueForPattern() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableUpdateLog(this);
    }
}
