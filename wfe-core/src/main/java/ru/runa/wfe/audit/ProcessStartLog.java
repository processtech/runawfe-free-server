package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface ProcessStartLog extends ProcessLog {

    @Transient
    String getActorName();
}
