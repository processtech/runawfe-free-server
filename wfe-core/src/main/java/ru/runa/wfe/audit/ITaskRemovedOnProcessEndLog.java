package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;

public interface ITaskRemovedOnProcessEndLog extends ITaskEndLog {

    @Transient
    default Long getEndedProcessId() {
        return Long.parseLong(getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ProcessIdValue(getEndedProcessId()) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskRemovedOnProcessEndLog(this);
    }
}
