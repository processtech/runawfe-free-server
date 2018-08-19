package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ITaskCancelledLog extends ITaskEndLog {

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
