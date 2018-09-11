package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;

@Entity
@DiscriminatorValue(value = "5")
public class ArchivedTaskEscalationLog extends ArchivedTaskLog implements TaskEscalationLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_ESCALATION;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorIdsValue(getAttributeNotNull(ATTR_MESSAGE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEscalationLog(this);
    }
}
