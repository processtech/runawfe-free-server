package ru.runa.wfe.audit;

public interface ITaskEndBySubstitutorLog extends ITaskEndLog {

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndBySubstitutorLog(this);
    }
}
