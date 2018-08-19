package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface IProcessStartLog extends IProcessLog {

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
        visitor.onProcessStartLog(this);
    }
}
