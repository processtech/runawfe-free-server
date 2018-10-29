package ru.runa.wfe.lang.jpdl;

import com.google.common.collect.Maps;
import java.util.Map;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

/**
 * Launch child tokens from the fork over the leaving transitions.
 */
public class Fork extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.FORK;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        CurrentToken token = executionContext.getCurrentToken();
        checkCyclicExecution(token);
        Map<CurrentToken, Transition> childTokens = Maps.newHashMap();
        for (Transition leavingTransition : getLeavingTransitions()) {
            CurrentToken childToken = new CurrentToken(token, getNodeId() + "/" + leavingTransition.getNodeId());
            childTokens.put(childToken, leavingTransition);
        }
        ApplicationContextFactory.getCurrentTokenDao().flushPendingChanges();
        log.debug("Child tokens created: " + childTokens.keySet());
        for (Map.Entry<CurrentToken, Transition> entry : childTokens.entrySet()) {
            ExecutionContext childExecutionContext = new ExecutionContext(executionContext.getParsedProcessDefinition(), entry.getKey());
            leave(childExecutionContext, entry.getValue());
        }
    }

    private void checkCyclicExecution(CurrentToken token) {
        if (token.getDepth() > SystemProperties.getTokenMaximumDepth()) {
            throw new RuntimeException("Cyclic fork execution does not allowed");
        }
    }
}
