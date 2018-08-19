package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface IProcessCancelLog extends IProcessLog {

    @Transient
    default String getActorName() {
        return getAttribute(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessCancelLog(this);
    }
}
