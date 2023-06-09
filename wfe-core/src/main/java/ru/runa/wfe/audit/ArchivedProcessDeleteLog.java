package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ArPIDel")
public class ArchivedProcessDeleteLog extends ProcessDeleteLog {

    protected ArchivedProcessDeleteLog() {
    }

    public ArchivedProcessDeleteLog(Long actorId, String name, Long processId) {
        super(actorId, name, processId);
    }
}
