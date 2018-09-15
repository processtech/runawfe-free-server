package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "K")
public class ArchivedTaskEndByAdminLog extends ArchivedTaskEndLog implements TaskEndByAdminLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_END_BY_ADMIN;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndByAdminLog(this);
    }
}
