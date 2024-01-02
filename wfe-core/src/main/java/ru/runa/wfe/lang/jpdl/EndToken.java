package ru.runa.wfe.lang.jpdl;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.val;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.CurrentToken;
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
    public Transition addLeavingTransition(Transition t) {
        throw new UnsupportedOperationException("can't add a leaving transition to " + this);
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        val executionLogic = ApplicationContextFactory.getExecutionLogic();
        executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null, null, false);
        if (!executionContext.getProcess().hasEnded() && executionContext.getCurrentProcess().getRootToken().hasEnded()) {
            executionLogic.endProcess(executionContext.getCurrentProcess(), executionContext, null);
        }
        // If this token was forked
        CurrentToken parentToken = executionContext.getCurrentToken().getParent();
        if (parentToken != null && parentToken.getNodeType() == NodeType.FORK && parentToken.getActiveChildren(false).size() == 0) {
            Set<Join> joins = Sets.newHashSet();
            for (CurrentToken childToken : parentToken.getChildren()) {
                if (childToken.getNodeType() == NodeType.JOIN) {
                    joins.add((Join) childToken.getNodeNotNull(executionContext.getParsedProcessDefinition()));
                }
            }
            for (Join join : joins) {
                join.execute(executionContext);
            }
        }
    }
}
