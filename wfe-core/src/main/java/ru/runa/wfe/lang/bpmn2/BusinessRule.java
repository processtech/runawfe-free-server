package ru.runa.wfe.lang.bpmn2;

import lombok.Setter;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.var.BusinessRuleHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class BusinessRule extends Node {
    private static final long serialVersionUID = 1235L;
    @Setter
    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.BUSINESS_RULE;
    }

    @Override
    public void validate() {
        super.validate();
        if (delegation == null) {
            throw new IllegalStateException("delegation cannot be null");
        } else {
            delegation.validate();
        }
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        if (delegation == null) {
            throw new IllegalStateException("delegation cannot be null");
        } else {
            BusinessRuleHandler businessRuleHandler = delegation.getInstance();
            businessRuleHandler.execute(executionContext);
            Transition transition = getDefaultLeavingTransitionNotNull();
            leave(executionContext, transition);
        }
    }
}
