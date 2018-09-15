package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "9")
public class ArchivedTaskExpiredLog extends ArchivedTaskEndLog implements TaskExpiredLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_EXPIRED;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskExpiredLog(this);
    }
}
