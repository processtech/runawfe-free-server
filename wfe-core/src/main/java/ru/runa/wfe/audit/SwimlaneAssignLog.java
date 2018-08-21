package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface SwimlaneAssignLog extends ProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.SWIMLANE_ASSIGN;
    }

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
