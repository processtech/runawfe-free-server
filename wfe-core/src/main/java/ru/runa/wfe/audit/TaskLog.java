package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskLog extends ProcessLog {

    @Transient
    Long getTaskId();

    @Transient
    String getTaskName();

    @Transient
    Integer getTaskIndex();

    @Transient
    String getSwimlaneName();

}
