package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;

@Entity
@DiscriminatorValue(value = "M")
public class ArchivedTaskCancelledByProcessEndLog extends ArchivedTaskCancelledLog implements TaskCancelledByProcessEndLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_REMOVED_ON_PROCESS_END;
    }

    @Override
    @Transient
    public Long getEndedProcessId() {
        return Long.parseLong(getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ProcessIdValue(getEndedProcessId()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
