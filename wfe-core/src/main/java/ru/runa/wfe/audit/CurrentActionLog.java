package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.GraphElement;

/**
 * Logging action execution.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "A")
public class CurrentActionLog extends CurrentProcessLog implements ActionLog {
    private static final long serialVersionUID = 1L;

    public CurrentActionLog() {
    }

    public CurrentActionLog(GraphElement action) {
        setNodeId(action.getNodeId());
        addAttributeWithTruncation(ATTR_ACTION, action.toString());
    }

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
