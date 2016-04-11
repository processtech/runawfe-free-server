package ru.runa.wfe.lang.bpmn2;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.DecisionHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class ExclusiveGateway extends Node {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.EXCLUSIVE_GATEWAY;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        if (getLeavingTransitions().size() > 1) {
            Preconditions.checkNotNull(delegation, "delegation in " + this);
        }
        if (delegation != null) {
            delegation.validate();
        }
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        try {
            if (delegation != null) {
                log.debug("gateway " + name + " is treated as decision gateway");
                DecisionHandler decisionHandler = delegation.getInstance();
                String transitionName = decisionHandler.decide(executionContext);
                Preconditions.checkNotNull(transitionName, "Null transition name by condition");
                Transition transition = getLeavingTransitionNotNull(transitionName);
                log.debug("gateway " + name + " is taking '" + transition + "'");
                leave(executionContext, transition);
            } else {
                log.debug("gateway " + name + " is treated as merge gateway");
                leave(executionContext, getDefaultLeavingTransitionNotNull());
            }
        } catch (Exception exception) {
            throw Throwables.propagate(exception);
        }
    }

}
