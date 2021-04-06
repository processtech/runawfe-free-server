package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.DecisionHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Preconditions;

/**
 * decision node.
 */
public class Decision extends Node {
    private static final long serialVersionUID = 1L;

    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.DECISION;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation in " + this);
        delegation.validate();
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        DecisionHandler decisionHandler = delegation.getInstance();
        String transitionName = decisionHandler.decide(executionContext);
        Preconditions.checkNotNull(transitionName, "Null transition name by condition");
        Transition transition = getLeavingTransitionNotNull(transitionName);
        log.debug("decision " + name + " is taking '" + transition + "'");
        leave(executionContext, transition);
    }

}
