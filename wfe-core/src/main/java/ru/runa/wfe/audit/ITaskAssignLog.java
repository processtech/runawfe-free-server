package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface ITaskAssignLog extends ITaskLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK_ASSIGN;
    }

    @Transient
    default String getOldExecutorName() {
        return getAttribute(ATTR_OLD_VALUE);
    }

    @Transient
    default String getNewExecutorName() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskAssignLog(this);
    }
}
