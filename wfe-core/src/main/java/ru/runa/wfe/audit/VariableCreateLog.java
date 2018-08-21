package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.FileValue;

public interface VariableCreateLog extends VariableLog {

    @Override
    @Transient
    default Type getType() {
        return Type.VARIABLE_CREATE;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        if (isFileValue()) {
            return new Object[] { getVariableName(), new FileValue(getId(), getVariableNewValueAttribute()) };
        }
        return new Object[] { getVariableName(), getVariableNewValue() };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableCreateLog(this);
    }
}
