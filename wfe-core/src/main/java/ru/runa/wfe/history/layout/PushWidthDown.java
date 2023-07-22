package ru.runa.wfe.history.layout;

import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;

/**
 * Moving down by the graph and push bigger width from parent node to child
 * node. It's require for better layout in case of different node graphical
 * elements size.
 */
public class PushWidthDown implements HistoryGraphNodeVisitor<Integer> {

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, Integer context) {
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, -1);
        }
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, Integer context) {
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, -1);
        }
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, Integer context) {
        int width = -1;
        if (!node.isForkNode()) {
            width = 0;
            for (HistoryGraphTransitionModel transition : node.getIncomingTransitions()) {
                width += NodeLayoutData.get(transition.getFromNode()).getSubtreeWidth();
            }
            NodeLayoutData layoutData = NodeLayoutData.get(node);
            layoutData.setSubtreeWidth(width);
            layoutData.setPreferredWidth(width - HistoryGraphLayoutProperties.widthBetweenNodes);
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, width);
        }
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, Integer context) {
        NodeLayoutData layoutData = NodeLayoutData.get(node);
        if (context > 0) {
            layoutData.setSubtreeWidth(context);
        } else {
            context = layoutData.getSubtreeWidth();
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
    }
}
