package ru.runa.wfe.graph.view;

import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.bpmn2.TimerNode;

public class TimerNodeGraphElement extends NodeGraphElement implements DelegableNodeGraphElement {

    private static final long serialVersionUID = 1L;

    private String duration;
    private String handlerName = "";
    private String handlerConfiguration = "";

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        TimerNode timer = (TimerNode) node;
        duration = timer.getDueDateExpression();
        Delegation delegation = timer.getActionDelegation();
        if (delegation != null) {
            handlerName = delegation.getClassName();
            handlerConfiguration = delegation.getConfiguration();
        }
    }

    public String getDuration() {
        return duration;
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
