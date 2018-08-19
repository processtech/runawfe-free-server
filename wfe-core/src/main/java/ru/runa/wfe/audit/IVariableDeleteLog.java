package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface IVariableDeleteLog extends IVariableLog {

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getVariableName() };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableDeleteLog(this);
    }
}
