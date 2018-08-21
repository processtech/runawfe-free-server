package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskCancelledLog extends TaskEndLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK_CANCELLED;
    }

    @Transient
    default String getHandlerInfo() {
        String handlerInfo = getAttribute(ATTR_INFO);
        if (handlerInfo != null) {
            return handlerInfo;
        }
        // for pre 4.1.0 data
        return "";
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), getHandlerInfo() };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
