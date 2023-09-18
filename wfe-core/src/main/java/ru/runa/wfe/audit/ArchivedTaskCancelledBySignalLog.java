package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "d")
public class ArchivedTaskCancelledBySignalLog extends ArchivedTaskCancelledLog implements TaskCancelledBySignalLog {
    private static final long serialVersionUID = 1L;

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {getTaskName()};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
