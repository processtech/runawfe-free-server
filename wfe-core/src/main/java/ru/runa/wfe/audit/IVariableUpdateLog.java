package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.FileValue;

public interface IVariableUpdateLog extends IVariableLog {

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
        visitor.onVariableUpdateLog(this);
    }
}
