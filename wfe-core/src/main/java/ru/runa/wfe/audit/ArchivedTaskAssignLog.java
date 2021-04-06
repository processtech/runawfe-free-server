package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "2")
public class ArchivedTaskAssignLog extends ArchivedTaskLog implements TaskAssignLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_ASSIGN;
    }

    @Override
    @Transient
    public String getOldExecutorName() {
        return getAttribute(ATTR_OLD_VALUE);
    }

    @Override
    @Transient
    public String getNewExecutorName() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    public String getExecutorIds() {
        return getAttribute(ATTR_MESSAGE);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)), new ExecutorIdsValue(getExecutorIds()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskAssignLog(this);
    }
}
