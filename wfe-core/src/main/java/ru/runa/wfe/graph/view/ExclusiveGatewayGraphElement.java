package ru.runa.wfe.graph.view;

import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.bpmn2.BusinessRule;
import ru.runa.wfe.lang.bpmn2.ExclusiveGateway;

public class ExclusiveGatewayGraphElement extends NodeGraphElement implements DelegableNodeGraphElement {

    private static final long serialVersionUID = 1L;

    private String handlerName = "";

    private String handlerConfiguration = "";

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        Delegation delegation = node instanceof ExclusiveGateway ? ((ExclusiveGateway) node).getDelegation() : ((BusinessRule) node).getDelegation();
        if (delegation != null) {
            handlerName = delegation.getClassName();
            handlerConfiguration = delegation.getConfiguration();
        }
    }

    @Override
    public String getHandlerName() {
        return handlerName;
    }

    @Override
    public String getHandlerConfiguration() {
        return handlerConfiguration;
    }

}
