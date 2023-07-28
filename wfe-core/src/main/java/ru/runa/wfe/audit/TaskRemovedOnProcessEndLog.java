package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskRemovedOnProcessEndLog extends TaskCancelledLog {

    @Transient
    Long getEndedProcessId();
}
