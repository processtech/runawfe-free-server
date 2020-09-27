package ru.runa.wfe.lang.bpmn2;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class DataStore extends Node {
    @Override
    public NodeType getNodeType() {
        return NodeType.TEXT_ANNOTATION;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        throw new UnsupportedOperationException();
    }
}
