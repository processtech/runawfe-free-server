package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface NodeEnterLog extends NodeLog {

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
