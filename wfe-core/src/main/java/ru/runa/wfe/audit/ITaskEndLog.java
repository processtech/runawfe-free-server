package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface ITaskEndLog extends ITaskLog {

    @Transient
    default String getActorName() {
        String actorName = getAttribute(ATTR_ACTOR_NAME);
        if (actorName != null) {
            return actorName;
        }
        return "";
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getActorName()) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndLog(this);
    }
}
