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
@DiscriminatorValue(value = "8")
public class CurrentReceiveMessageLog extends CurrentMessageNodeLog implements ReceiveMessageLog {
    private static final long serialVersionUID = 1L;

    public CurrentReceiveMessageLog() {
    }

    public CurrentReceiveMessageLog(BaseMessageNode node, String message) {
        super(node, message);
    }

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
