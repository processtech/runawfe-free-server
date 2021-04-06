package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;

/**
 * Logging node entering.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "N")
public class CurrentNodeEnterLog extends CurrentNodeLog implements NodeEnterLog {
    private static final long serialVersionUID = 1L;

    public CurrentNodeEnterLog() {
    }

    public CurrentNodeEnterLog(Node node) {
        super(node);
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.NODE_ENTER;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeEnterLog(this);
    }
}
