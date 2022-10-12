package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "8")
public class ArchivedReceiveMessageLog extends ArchivedMessageNodeLog implements ReceiveMessageLog {

    @Override
    @Transient
    public Type getType() {
        return Type.RECEIVED_MESSAGE;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onReceiveMessageLog(this);
    }
}
