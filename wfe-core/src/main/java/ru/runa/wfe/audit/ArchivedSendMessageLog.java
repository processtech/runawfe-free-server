package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "7")
public class ArchivedSendMessageLog extends ArchivedMessageNodeLog implements SendMessageLog {

    @Override
    @Transient
    public Type getType() {
        return Type.SEND_MESSAGE;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSendMessageLog(this);
    }
}
