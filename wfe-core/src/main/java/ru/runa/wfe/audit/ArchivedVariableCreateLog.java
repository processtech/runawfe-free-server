package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.FileValue;

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
        if (isFileValue()) {
            return new Object[] { getVariableName(), new FileValue(getId(), getVariableNewValueAttribute()) };
        }
        return new Object[] { getVariableName(), getVariableNewValue() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableCreateLog(this);
    }
}
