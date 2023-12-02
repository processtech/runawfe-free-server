package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "F")
public class ArchivedTaskDelegationLog extends ArchivedTaskLog implements TaskDelegationLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_DELEGATION;
    }

    @Override
    @Transient
    public String getExecutorIds() {
        return getAttributeNotNull(ATTR_MESSAGE);
    }

    @Override
    @Transient
    public String getActorName() {
        return getAttribute(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    public Long getActorId() {
        String actorIdString = getAttribute(ATTR_ACTOR_ID);
        return actorIdString != null ? Long.parseLong(actorIdString) : null;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorIdsValue(getExecutorIds()), new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskDelegationLog(this);
    }
}
