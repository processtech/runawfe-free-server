package ru.runa.wfe.lang.bpmn2;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class TextAnnotation extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.TEXT_ANNOTATION;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        throw new UnsupportedOperationException();
    }

}
