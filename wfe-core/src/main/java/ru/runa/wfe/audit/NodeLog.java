package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public interface NodeLog extends ProcessLog {

    @Transient
    String getNodeName();

    @Transient
    NodeType getNodeType();

    /**
     * Available only out of persistence context
     */
    @Transient
    public Node getNode();

}
