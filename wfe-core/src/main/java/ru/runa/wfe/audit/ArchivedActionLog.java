package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "A")
public class ArchivedActionLog extends ArchivedProcessLog implements ActionLog {

    @Override
    @Transient
    public Type getType() {
        return Type.ACTION;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_ACTION) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onActionLog(this);
    }
}
