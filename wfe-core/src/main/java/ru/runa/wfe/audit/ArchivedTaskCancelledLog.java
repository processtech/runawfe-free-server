package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "O")
public class ArchivedTaskCancelledLog extends ArchivedTaskEndLog implements TaskCancelledLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_CANCELLED;
    }

    @Override
    @Transient
    public String getHandlerInfo() {
        String handlerInfo = getAttribute(ATTR_INFO);
        if (handlerInfo != null) {
            return handlerInfo;
        }
        // for pre 4.1.0 data
        return "";
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), getHandlerInfo() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
