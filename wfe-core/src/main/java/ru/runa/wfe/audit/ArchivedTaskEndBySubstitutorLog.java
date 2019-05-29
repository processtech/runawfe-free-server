package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "S")
public class ArchivedTaskEndBySubstitutorLog extends ArchivedTaskEndLog implements TaskEndBySubstitutorLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_END_BY_SUBSTITUTOR;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndBySubstitutorLog(this);
    }
}
