package ru.runa.wfe.audit;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class ArchivedTaskLog extends ArchivedProcessLog implements TaskLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK;
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

    @Override
    @Transient
    public String getSwimlaneName() {
        return super.getSwimlaneName() != null ? super.getSwimlaneName() : getAttribute(ATTR_SWIMLANE_NAME);
    }
}
