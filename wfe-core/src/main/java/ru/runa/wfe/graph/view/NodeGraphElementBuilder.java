package ru.runa.wfe.graph.view;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;

public class NodeGraphElementBuilder {

    /**
     * Convert nodes to graph elements.
     *
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public static List<NodeGraphElement> createElements(ProcessDefinition definition) {
        List<NodeGraphElement> result = Lists.newArrayList();
        List<Node> nodes = definition.getNodes(false);
        nodes.sort(new NodeChidrenFirstComparator());
        for (Node node : nodes) {
            result.add(createElement(node));
        }
        return result;
    }

    /**
     * Convert nodes to graph elements.
     *
     * @param definitionNodes
     *            Nodes to convert
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

    private static class NodeChidrenFirstComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            int p1 = o1.getParentElement() instanceof ProcessDefinition ? 1 : 0;
            int p2 = o2.getParentElement() instanceof ProcessDefinition ? 1 : 0;
            return Integer.compare(p1, p2);
        }

    }
}
