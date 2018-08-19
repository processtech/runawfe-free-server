package ru.runa.wfe.audit;

public interface ITaskEndByAdminLog extends ITaskEndLog {

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndByAdminLog(this);
    }
}
