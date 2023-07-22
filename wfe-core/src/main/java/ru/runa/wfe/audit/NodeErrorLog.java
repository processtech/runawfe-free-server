package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;

/**
 * Logging node errors
 *
 * @author Alekseev Mikhail
 * @since #1923
 */
@Entity
@DiscriminatorValue(value = "H")
public class NodeErrorLog extends NodeLog {
    private static final long serialVersionUID = 3940080812294087447L;

    public NodeErrorLog() {
    }

    public NodeErrorLog(Node node, String message) {
        super(node);
        setSeverity(Severity.ERROR);
        addAttributeWithTruncation(ATTR_MESSAGE, message);
    }

    public NodeErrorLog(Node node, String message, byte[] bytes) {
        this(node, message);
        setBytes(bytes);
    }

    @Transient
    public String getMessage() {
        return getAttributeNotNull(ATTR_MESSAGE);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeErrorLog(this);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[]{ getMessage() };
    }
}
