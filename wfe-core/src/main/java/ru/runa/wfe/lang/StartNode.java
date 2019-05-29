package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;

public class StartNode extends InteractionNode {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.START_EVENT;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
    }
}
