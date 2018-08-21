package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface IProcessEndLog extends IProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.PROCESS_END;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] {};
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessEndLog(this);
    }
}
