package ru.runa.wfe.graph.view;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;

public class NodeGraphElementBuilder {

    /**
     * Convert nodes to graph elements.
     *
     * @return List of graph elements for nodes.
     */
    public static List<NodeGraphElement> createElements(ProcessDefinition definition) {
        List<NodeGraphElement> result = Lists.newArrayList();
        for (Node node : definition.getNodes(false)) {
            result.add(createElement(node));
        }
        return result;
    }

    /**
     * Convert nodes to graph elements.
     *
     * @return List of graph elements for nodes.
     */
    public static NodeGraphElement createElement(Node node) {
        NodeGraphElement element;
        switch (node.getNodeType()) {
        case START_EVENT:
            element = new StartNodeGraphElement();
            break;
        case TASK_STATE:
        case MULTI_TASK_STATE:
            element = new TaskNodeGraphElement();
            break;
        case SUBPROCESS:
            element = new SubprocessNodeGraphElement();
            break;
        case MULTI_SUBPROCESS:
            element = new MultiSubprocessNodeGraphElement();
            break;
        default:
            element = new NodeGraphElement();
        }
        int[] graphConstraints = new int[] { node.getGraphConstraints()[0], node.getGraphConstraints()[1],
                node.getGraphConstraints()[0] + node.getGraphConstraints()[2], node.getGraphConstraints()[1] + node.getGraphConstraints()[3] };
        element.initialize(node, graphConstraints);
        return element;
    }
}
