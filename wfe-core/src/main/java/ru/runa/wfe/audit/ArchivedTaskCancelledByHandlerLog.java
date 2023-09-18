package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "a")
public class ArchivedTaskCancelledByHandlerLog extends ArchivedTaskCancelledLog implements TaskCancelledByHandlerLog {
    private static final long serialVersionUID = 1L;

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {getTaskName(), getAttribute(ATTR_INFO)};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
