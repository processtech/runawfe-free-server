package ru.runa.wfe.audit;

import javax.persistence.Transient;

//TODO rm1085 divide to TaskCompletedByHandlerLog, TaskCompletedBySignalLog?
public interface TaskCancelledLog extends TaskEndLog {

    @Transient
    String getHandlerInfo();
}
