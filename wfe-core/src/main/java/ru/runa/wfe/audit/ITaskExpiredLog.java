package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ITaskExpiredLog extends ITaskEndLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK_EXPIRED;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName() };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskExpiredLog(this);
    }
}
