package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;

/**
 * Logging node leaving.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "L")
public class CurrentNodeLeaveLog extends CurrentNodeLog implements NodeLeaveLog {
    private static final long serialVersionUID = 1L;

    public CurrentNodeLeaveLog() {
    }

    public CurrentNodeLeaveLog(Node node) {
        super(node);
    }

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
