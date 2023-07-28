package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "D")
public class ArchivedVariableDeleteLog extends ArchivedVariableLog implements VariableDeleteLog {

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE_DELETE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getVariableName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableDeleteLog(this);
    }
}
