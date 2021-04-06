package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskAssignLog extends TaskLog {

    @Transient
    String getOldExecutorName();

    @Transient
    String getNewExecutorName();

    @Transient
    String getExecutorIds();

}
