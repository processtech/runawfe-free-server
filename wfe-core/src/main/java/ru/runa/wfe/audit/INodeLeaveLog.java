package ru.runa.wfe.audit;

public interface INodeLeaveLog extends INodeLog {

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeLeaveLog(this);
    }
}
