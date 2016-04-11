package ru.runa.wfe.graph.view;

import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;

public class GraphElementPresentationBuilder {

    /**
     * Convert nodes to graph elements.
     * 
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public static List<GraphElementPresentation> createElements(ProcessDefinition definition) {
        List<GraphElementPresentation> result = Lists.newArrayList();
        for (Node node : definition.getNodes(false)) {
            GraphElementPresentation presentation;
            switch (node.getNodeType()) {
            case SUBPROCESS:
                presentation = new SubprocessGraphElementPresentation();
                break;
            case MULTI_SUBPROCESS:
                presentation = new MultiinstanceGraphElementPresentation();
                break;
            case TASK_STATE:
                presentation = new TaskGraphElementPresentation();
                break;
            default:
                presentation = new GraphElementPresentation();
            }
            int[] graphConstraints = new int[]{
                    node.getGraphConstraints()[0], node.getGraphConstraints()[1],
                    node.getGraphConstraints()[0] + node.getGraphConstraints()[2],
                    node.getGraphConstraints()[1] + node.getGraphConstraints()[3]
            };
            presentation.initialize(node, graphConstraints);
            result.add(presentation);
        }
        return result;
    }
}
