package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface TaskEndLog extends TaskLog {

    @Transient
    String getActorName();
}
