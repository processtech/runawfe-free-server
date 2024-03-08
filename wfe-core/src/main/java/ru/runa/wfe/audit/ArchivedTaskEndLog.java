package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "3")
public class ArchivedTaskEndLog extends ArchivedTaskLog implements TaskEndLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_END;
    }

    @Override
    @Transient
    public String getActorName() {
        return getExecutorNameOrNull();
    }

    @Override
    @Transient
    public String getTransitionName() {
        return getAttribute(ATTR_TRANSITION_NAME);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndLog(this);
    }
}
