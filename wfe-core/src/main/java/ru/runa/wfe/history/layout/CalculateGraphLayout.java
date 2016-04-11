package ru.runa.wfe.history.layout;

import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;
import ru.runa.wfe.history.layout.CalculateGraphLayoutContext.SubtreeOffset;

/**
 * Calculates history graph layout. Before calling this operation
 * {@link CalculateSubTreeBounds} must be called.
 */
public class CalculateGraphLayout implements HistoryGraphNodeVisitor<CalculateGraphLayoutContext> {

    public CalculateGraphLayout() {
        super();
    }

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, CalculateGraphLayoutContext context) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.positionCalulationRequired()) {
            return;
        }
        context.calculateNodePosition(data);
        if (data.getHeight() > HistoryGraphLayoutProperties.joinHeight) {
            data.setHeight(HistoryGraphLayoutProperties.joinHeight);
        }
        int subtreesOffset = data.getSubtreeWidth();
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            subtreesOffset -= NodeLayoutData.get(transition.getToNode()).getSubtreeWidth();
        }
        int widthOffset = context.getWidth();
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            context.setSubtreeOffset(node, context.getSubtreeOffset() + subtreesOffset / 2);
            transition.getToNode().processBy(this, context.createNew(widthOffset));
            widthOffset += NodeLayoutData.get(transition.getToNode()).getSubtreeWidth();
            context.resetSubtreeOffset(node);
        }
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, CalculateGraphLayoutContext context) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.positionCalulationRequired()) {
            return;
        }
        SubtreeOffset storedOffset = context.resetSubtreeOffset();
        context.calculateNodePosition(data);
        if (data.getHeight() > HistoryGraphLayoutProperties.joinHeight) {
            data.setHeight(HistoryGraphLayoutProperties.joinHeight);
        }
        HistoryGraphTransitionModel transition = node.getTransitions().size() == 0 ? null : node.getTransitions().get(0);
        if (transition != null) {
            int widthOffset = context.getWidth();
            if (data.getSubtreeWidth() > NodeLayoutData.get(transition.getToNode()).getSubtreeWidth()) {
                widthOffset += (data.getSubtreeWidth() - NodeLayoutData.get(transition.getToNode()).getSubtreeWidth()) / 2;
            }
            transition.getToNode().processBy(this, context.createNew(widthOffset));
        }
        context.setSubtreeOffset(storedOffset.initiatedNode, storedOffset.offset);
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, CalculateGraphLayoutContext context) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.positionCalulationRequired()) {
            return;
        }
        context.calculateNodePosition(data);
        if (data.getHeight() > HistoryGraphLayoutProperties.joinHeight) {
            data.setHeight(HistoryGraphLayoutProperties.joinHeight);
        }
        int widthOffset = context.getWidth();
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context.createNew(widthOffset));
            widthOffset += NodeLayoutData.get(transition.getToNode()).getSubtreeWidth();
        }
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, CalculateGraphLayoutContext context) {
        NodeLayoutData data = NodeLayoutData.get(node);
        if (!data.positionCalulationRequired()) {
            return;
        }
        context.calculateNodePosition(data);
        int widthOffset = context.getWidth();
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context.createNew(widthOffset));
            widthOffset += NodeLayoutData.get(transition.getToNode()).getSubtreeWidth();
        }
    }
}
