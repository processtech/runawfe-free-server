package ru.runa.wfe.graph.view;

import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ScriptNode;

public class ScriptNodeGraphElement extends NodeGraphElement implements DelegableNodeGraphElement {

    private static final long serialVersionUID = 1L;

    private boolean minimized;

    private String handlerName = "";

    private String handlerConfiguration = "";

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        Delegation delegation = ((ScriptNode) node).getDelegation();
        minimized = node.isGraphMinimizedView();
        if (delegation != null) {
            handlerName = delegation.getClassName();
            handlerConfiguration = delegation.getConfiguration();
        }
    }

    public boolean isMinimized() {
        return minimized;
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
