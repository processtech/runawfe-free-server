package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface INodeEnterLog extends INodeLog {

    @Override
    @Transient
    default Type getType() {
        return Type.NODE_ENTER;
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeEnterLog(this);
    }
}
