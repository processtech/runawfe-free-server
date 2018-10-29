package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class ReceiveMessageNode extends BaseMessageNode {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.getCurrentToken().setMessageSelector(Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), this));
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        super.leave(executionContext, transition);
        executionContext.getCurrentToken().setMessageSelector(null);
    }
}
