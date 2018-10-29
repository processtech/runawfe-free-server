package ru.runa.wfe.graph.view;

import java.util.List;

import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

/**
 * Represents an StartNode graph element.
 */
public class StartNodeGraphElement extends NodeGraphElement {

    private static final long serialVersionUID = 1L;

    /**
     * Swimlane name of this task element.
     */
    private String swimlaneName;

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        List<TaskDefinition> taskDefinitions = ((InteractionNode) node).getTasks();
        if (taskDefinitions.size() > 0) {
            // none for EmbeddedSubprocessStartNode
            swimlaneName = taskDefinitions.get(0).getSwimlane().getName();
        }
    }

    /**
     * Swimlane name of this task element.
     */
    public String getSwimlaneName() {
        return swimlaneName;
    }

}
