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

    public CurrentNodeLog() {
    }

    public CurrentNodeLog(Node node) {
        setNodeId(node.getNodeId());
        addAttribute(ATTR_NODE_NAME, node.getName());
        addAttribute(ATTR_NODE_TYPE, node.getNodeType().name());
    }

    @Override
    @Transient
    public Type getType() {
        return Type.NODE;
    }

    @Override
    @Transient
    public String getNodeName() {
        return getAttributeNotNull(ATTR_NODE_NAME);
    }

    @Override
    @Transient
    public NodeType getNodeType() {
        return NodeType.valueOf(getAttributeNotNull(ATTR_NODE_TYPE));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getNodeName() };
    }
}
