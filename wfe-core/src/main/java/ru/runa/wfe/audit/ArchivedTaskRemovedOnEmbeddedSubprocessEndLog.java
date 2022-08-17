package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "P")
public class ArchivedTaskRemovedOnEmbeddedSubprocessEndLog extends ArchivedTaskCancelledLog implements TaskRemovedOnEmbeddedSubprocessEndLog {
    private static final long serialVersionUID = 1L;


    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskRemovedOnEmbeddedSubprocessEndLog(this);
    }
}
