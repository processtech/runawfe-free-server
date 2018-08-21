package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface NodeLeaveLog extends NodeLog {

    @Override
    @Transient
    default Type getType() {
        return Type.NODE_LEAVE;
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeLeaveLog(this);
    }
}
