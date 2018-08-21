package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ITaskLog extends IProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK;
    }

    @Transient
    default Long getTaskId() {
        String taskIdString = getAttribute(ATTR_TASK_ID);
        if (taskIdString != null) {
            return Long.parseLong(taskIdString);
        }
        return null;
    }

    @Transient
    default String getTaskName() {
        return getAttributeNotNull(ATTR_TASK_NAME);
    }

    @Transient
    default Integer getTaskIndex() {
        String taskIndexString = getAttribute(ATTR_INDEX);
        if (taskIndexString != null) {
            return Integer.valueOf(taskIndexString);
        }
        return null;
    }
}
