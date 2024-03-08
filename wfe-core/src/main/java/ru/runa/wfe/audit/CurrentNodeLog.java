package ru.runa.wfe.audit;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

/**
 * Logging node execution.
 *
 * @author Dofs
 */
@MappedSuperclass
public abstract class CurrentNodeLog extends CurrentProcessLog implements NodeLog {
    private static final long serialVersionUID = 1L;
    private transient Node node;

    public CurrentNodeLog() {
    }

    public CurrentNodeLog(Node node) {
        this.node = node;
        setNodeId(node.getNodeId());
        setNodeName(node.getName());
        addAttribute(ATTR_NODE_TYPE, node.getNodeType().name());
        setSeverity(Severity.INFO);
    }

    /**
     * Available only out of persistence context
     */
    @Override
    @Transient
    public Node getNode() {
        return node;
    }

    @Override
    @Transient
    public Type getType() {
        return Type.NODE;
    }

    @Override
    @Transient
    public NodeType getNodeType() {
        return NodeType.valueOf(getAttributeNotNull(ATTR_NODE_TYPE));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getNodeNameNotNull() };
    }
}
