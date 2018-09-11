package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ProcessActivateLog extends ProcessLog {

    @Transient
    String getActorName();
}
