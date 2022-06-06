package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.BaseMessageNode;

/**
 * Logging message nodes execution.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "7")
public class CurrentSendMessageLog extends CurrentMessageNodeLog implements SendMessageLog {
    private static final long serialVersionUID = 1L;

    public CurrentSendMessageLog() {
    }

    public CurrentSendMessageLog(BaseMessageNode node, String message) {
        super(node, message);
    }

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
