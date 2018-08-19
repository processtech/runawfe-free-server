package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface ISwimlaneAssignLog extends IProcessLog {

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_MESSAGE), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onSwimlaneAssignLog(this);
    }
}
