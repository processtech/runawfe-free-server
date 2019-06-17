package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "J")
public abstract class ArchivedTaskLog extends ArchivedProcessLog implements TaskLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK;
    }

    @Override
    @Transient
    public Long getTaskId() {
        String taskIdString = getAttribute(ATTR_TASK_ID);
        if (taskIdString != null) {
            return Long.parseLong(taskIdString);
        }
        return null;
    }

    @Override
    @Transient
    public String getTaskName() {
        return getAttributeNotNull(ATTR_TASK_NAME);
    }

    @Override
    @Transient
    public Integer getTaskIndex() {
        String taskIndexString = getAttribute(ATTR_INDEX);
        if (taskIndexString != null) {
            return Integer.valueOf(taskIndexString);
        }
        return null;
    }
}
