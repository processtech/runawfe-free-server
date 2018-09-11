package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "L")
public class ArchivedNodeLeaveLog extends ArchivedNodeLog implements NodeLeaveLog {

    @Override
    @Transient
    public Type getType() {
        return Type.NODE_LEAVE;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeLeaveLog(this);
    }
}
