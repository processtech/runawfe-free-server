package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskDelegationLog extends TaskLog {

    @Transient
    String getExecutorIds();

    @Transient
    Long getActorId();
}
