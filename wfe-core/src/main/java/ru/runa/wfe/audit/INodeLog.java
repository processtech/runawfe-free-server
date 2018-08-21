package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.lang.NodeType;

public interface INodeLog extends IProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.NODE;
    }

    @Transient
    default String getNodeName() {
        return getAttributeNotNull(ATTR_NODE_NAME);
    }

    @Transient
    default NodeType getNodeType() {
        return NodeType.valueOf(getAttributeNotNull(ATTR_NODE_TYPE));
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getNodeName() };
    }

}
