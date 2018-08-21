package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ActionLog extends ProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.ACTION;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_ACTION) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onActionLog(this);
    }
}
