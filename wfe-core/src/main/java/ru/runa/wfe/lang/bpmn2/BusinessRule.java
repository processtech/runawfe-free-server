package ru.runa.wfe.lang.bpmn2;

import com.google.common.base.Preconditions;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.DecisionHandler;
import ru.runa.wfe.extension.handler.var.BusinessRuleHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class BusinessRule extends Node {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.BUSINESS_RULE;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        if (delegation != null) {
            delegation.validate();
        }
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        if (delegation != null) {
            BusinessRuleHandler businessRuleHandler = delegation.getInstance();
            businessRuleHandler.execute(executionContext);
            Transition transition = getDefaultLeavingTransitionNotNull();
            leave(executionContext, transition);
        } else {
            throw new NullPointerException();
        }
    }
}
