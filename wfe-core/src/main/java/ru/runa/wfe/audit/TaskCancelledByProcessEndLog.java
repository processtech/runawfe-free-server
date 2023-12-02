package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskCancelledByProcessEndLog extends TaskCancelledLog {

    @Transient
    Long getEndedProcessId();

}
