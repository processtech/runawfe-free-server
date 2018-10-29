package ru.runa.wfe.lang.bpmn2;

import lombok.val;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class EndToken extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.END_TOKEN;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        val executionLogic = ApplicationContextFactory.getExecutionLogic();
        executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null, null, false);
        if (!executionContext.getProcess().hasEnded()) {
            val tokenDao = ApplicationContextFactory.getCurrentTokenDao();
            int count = tokenDao.findByProcessAndExecutionStatusIsNotEnded(executionContext.getCurrentProcess()).size();
            if (count == 0) {
                executionLogic.endProcess(executionContext.getCurrentProcess(), executionContext, null);
            }
        }
    }

    @Override
    public Transition addLeavingTransition(Transition t) {
        throw new UnsupportedOperationException("can't add a leaving transition to " + this);
    }
}
