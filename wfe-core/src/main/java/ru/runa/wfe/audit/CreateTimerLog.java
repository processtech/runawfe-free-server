package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface CreateTimerLog extends ProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.CREATE_TIMER;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerLog(this);
    }
}
