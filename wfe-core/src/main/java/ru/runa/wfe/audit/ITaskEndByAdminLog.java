package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ITaskEndByAdminLog extends ITaskEndLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK_END_BY_ADMIN;
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndByAdminLog(this);
    }
}
