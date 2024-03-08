package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "4")
public class ArchivedSwimlaneAssignLog extends ArchivedProcessLog implements SwimlaneAssignLog {

    @Override
    @Transient
    public Type getType() {
        return Type.SWIMLANE_ASSIGN;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getSwimlaneNameNotNull(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSwimlaneAssignLog(this);
    }

    @Transient
    private String getSwimlaneNameNotNull() {
        return super.getSwimlaneName() != null ? super.getSwimlaneName() : getAttributeNotNull(ATTR_MESSAGE);
    }
}
