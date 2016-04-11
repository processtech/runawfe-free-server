package ru.runa.wfe.history.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;

/**
 * Order transitions to get better graph.
 */
public class TransitionOrderer implements HistoryGraphNodeVisitor<TransitionOrdererContext> {

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, TransitionOrdererContext context) {
        return;
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, TransitionOrdererContext context) {
        return;
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, TransitionOrdererContext context) {
        if (!context.isReordering()) {
            context.setFindNode(node);
            return;
        }
        if (!node.isForkNode()) {
            moveForward(node, context);
        } else {
            reorderParallelFork(node, context);
        }
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
    }

    private void reorderParallelFork(HistoryGraphParallelNodeModel node, TransitionOrdererContext context) {
        List<HistoryGraphNode> complexFollowNodes = new ArrayList<HistoryGraphNode>();
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            TransitionOrdererContext searchContext = new TransitionOrdererContext(context);
            transition.getToNode().processBy(this, searchContext);
            complexFollowNodes.add(searchContext.getFindNode());
        }
        Map<HistoryGraphNode, Integer> nodeToHeight = new HashMap<HistoryGraphNode, Integer>();
        for (HistoryGraphNode historyGraphNode : complexFollowNodes) {
            if (historyGraphNode != null) {
                nodeToHeight.put(historyGraphNode, NodeLayoutData.get(historyGraphNode).getSubtreeHeight());
            }
        }
        List<Integer> newOrder = new ArrayList<Integer>();
        while (!nodeToHeight.isEmpty()) {
            int maxHeight = Collections.max(nodeToHeight.values());
            for (HistoryGraphNode historyGraphNode : nodeToHeight.keySet()) {
                if (nodeToHeight.get(historyGraphNode) == maxHeight) {
                    for (int i = 0; i < complexFollowNodes.size(); ++i) {
                        if (complexFollowNodes.get(i) == historyGraphNode) {
                            newOrder.add(i);
                        }
                    }
                    nodeToHeight.remove(historyGraphNode);
                    break;
                }
            }
        }
        for (int i = 0; i < complexFollowNodes.size(); ++i) {
            if (complexFollowNodes.get(i) == null) {
                newOrder.add(i);
            }
        }
        node.reorderTransitions(newOrder);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, TransitionOrdererContext context) {
        moveForward(node, context);
    }

    /**
     * Do nothing on current node and process all child nodes with current
     * operation.
     * 
     * @param node
     *            Current processing node.
     * @param context
     *            Operation context.
     */
    private void moveForward(HistoryGraphNode node, TransitionOrdererContext context) {
        List<HistoryGraphTransitionModel> transitions = node.getTransitions();
        if (transitions == null || transitions.size() == 0) {
            return;
        }
        transitions.get(0).getToNode().processBy(this, context);
    }
}
