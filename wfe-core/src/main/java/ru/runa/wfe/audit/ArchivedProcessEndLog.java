package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "X")
public class ArchivedProcessEndLog extends ArchivedProcessLog implements ProcessEndLog {

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_END;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessEndLog(this);
    }
}
