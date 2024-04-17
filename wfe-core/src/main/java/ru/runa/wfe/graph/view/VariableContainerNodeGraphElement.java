package ru.runa.wfe.graph.view;

import java.util.List;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.var.VariableMapping;

public class VariableContainerNodeGraphElement extends NodeGraphElement {

    private static final long serialVersionUID = 1L;

    private List<VariableMapping> variableMappings;

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        variableMappings = ((VariableContainerNode) node).getVariableMappings();
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

}
