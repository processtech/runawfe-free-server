package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;

public class WaitState extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.WAIT_STATE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        log.info("Waiting in " + this);
    }
}
