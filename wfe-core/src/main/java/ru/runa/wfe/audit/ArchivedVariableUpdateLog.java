package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.FileValue;

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
        if (isFileValue()) {
            return new Object[] { getVariableName(), new FileValue(getId(), getVariableNewValueAttribute()) };
        }
        return new Object[] { getVariableName(), getVariableNewValue() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableUpdateLog(this);
    }
}
