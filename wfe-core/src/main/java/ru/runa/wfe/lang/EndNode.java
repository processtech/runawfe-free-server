package ru.runa.wfe.lang;

import lombok.val;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionContext;

public class EndNode extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.END_PROCESS;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        val executionLogic = ApplicationContextFactory.getExecutionLogic();
        executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null, null, false);
        executionLogic.endProcess(executionContext.getCurrentProcess(), executionContext, null);
    }

    @Override
    public Transition addLeavingTransition(Transition t) {
        throw new UnsupportedOperationException("can't add a leaving transition to " + this);
    }
}
