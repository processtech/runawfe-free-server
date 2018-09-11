package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ProcessCancelLog extends ProcessLog {

    @Transient
    String getActorName();
}
