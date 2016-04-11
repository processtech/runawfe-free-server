package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class Conjunction extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.MERGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        log.debug("Executing " + this);
        leave(executionContext);
    }

}
