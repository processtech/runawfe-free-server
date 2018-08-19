package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface ITaskDelegationLog extends ITaskLog {

    @Transient
    default String getExecutorIds() {
        return getAttributeNotNull(ATTR_MESSAGE);
    }

    @Transient
    default String getActorName() {
        return getAttribute(ATTR_ACTOR_NAME);
    }

    @Transient
    default Long getActorId() {
        String actorIdString = getAttribute(ATTR_ACTOR_ID);
        return actorIdString != null ? Long.parseLong(actorIdString) : null;
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorIdsValue(getExecutorIds()), new ExecutorNameValue(getActorName()) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskDelegaionLog(this);
    }
}
