package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface INodeLeaveLog extends INodeLog {

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
