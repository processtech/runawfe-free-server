package ru.runa.wfe.graph.view;

import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

/**
 * Represents an task state graph element.
 */
public class TaskNodeGraphElement extends NodeGraphElement {

    private static final long serialVersionUID = 1L;

    /**
     * Flag, equals true, if task state is minimized; false otherwise.
     */
    private boolean minimized;

    /**
     * Swimlane name of this task element.
     */
    private String swimlaneName;

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
        if (null != taskDefinition.getSwimlane()) {
            swimlaneName = taskDefinition.getSwimlane().getName();
        }
        minimized = node.isGraphMinimizedView();
    }

    /**
     * Flag, equals true, if state is collapsed; false otherwise.
     */
    public boolean isMinimized() {
        return minimized;
    }

    /**
     * Swimlane name of this task element.
     */
    public String getSwimlaneName() {
        return swimlaneName;
    }

}
