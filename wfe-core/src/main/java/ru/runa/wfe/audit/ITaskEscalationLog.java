package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;

public interface ITaskEscalationLog extends ITaskLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK_ESCALATION;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorIdsValue(getAttributeNotNull(ATTR_MESSAGE)) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEscalationLog(this);
    }
}
