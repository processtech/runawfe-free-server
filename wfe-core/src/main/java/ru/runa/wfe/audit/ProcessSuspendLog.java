package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface ProcessSuspendLog extends ProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.PROCESS_SUSPEND;
    }

    @Transient
    default String getActorName() {
        return getAttributeNotNull(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getActorName()) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessSuspendLog(this);
    }
}
