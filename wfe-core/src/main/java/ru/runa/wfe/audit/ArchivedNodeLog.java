package ru.runa.wfe.audit;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

@MappedSuperclass
public abstract class ArchivedNodeLog extends ArchivedProcessLog implements NodeLog {

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
    public Node getNode() {
        return null;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getNodeName() };
    }
}
