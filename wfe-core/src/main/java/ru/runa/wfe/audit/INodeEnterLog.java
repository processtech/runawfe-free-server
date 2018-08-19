package ru.runa.wfe.audit;

public interface INodeEnterLog extends INodeLog {

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeEnterLog(this);
    }
}
