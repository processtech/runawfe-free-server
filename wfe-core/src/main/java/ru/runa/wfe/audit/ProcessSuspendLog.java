package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ProcessSuspendLog extends ProcessLog {

    @Transient
    String getActorName();
}
