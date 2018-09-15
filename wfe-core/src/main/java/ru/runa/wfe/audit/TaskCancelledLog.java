package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskCancelledLog extends TaskEndLog {

    @Transient
    String getHandlerInfo();
}
