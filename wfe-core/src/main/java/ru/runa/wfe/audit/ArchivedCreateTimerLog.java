package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "C")
@SuppressWarnings("unused")
public class ArchivedCreateTimerLog extends ArchivedProcessLog implements CreateTimerLog {

    @Override
    @Transient
    public Type getType() {
        return Type.CREATE_TIMER;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerLog(this);
    }
}
