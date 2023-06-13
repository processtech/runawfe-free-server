package ru.runa.wfe.graph.history;

import java.awt.Color;
import java.util.List;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.graph.image.figure.uml.UmlFigureFactory;
import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphGenericNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphJoinNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.graph.HistoryGraphNodeVisitor;
import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;
import ru.runa.wfe.history.graph.HistoryGraphTransitionModel;
import ru.runa.wfe.history.layout.NodeLayoutData;
import ru.runa.wfe.lang.Bendpoint;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;

/**
 * Operation to create figures for history graph painting.
 */
public class CreateGraphFigures implements HistoryGraphNodeVisitor<CreateGraphFiguresContext> {

    private final AbstractFigureFactory factory = new UmlFigureFactory();

    public CreateGraphFigures() {
        super();
    }

    @Override
    public void onForkNode(HistoryGraphForkNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        Node model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data);
    }

    @Override
    public void onJoinNode(HistoryGraphJoinNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        Node model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data);
    }

    @Override
    public void onParallelNode(HistoryGraphParallelNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        Node model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data);
    }

    @Override
    public void onGenericNode(HistoryGraphGenericNodeModel node, CreateGraphFiguresContext context) {
        FiguresNodeData data = FiguresNodeData.getOrCreate(node);
        if (!data.isFiguresInitializeRequired()) {
            return;
        }
        Node model = createCommonModel(node);
        createFigureForNode(node, data, model);
        for (HistoryGraphTransitionModel transition : node.getTransitions()) {
            transition.getToNode().processBy(this, context);
        }
        createFigureForTransitions(node, data);
    }

    private Node createCommonModel(HistoryGraphNode historyNode) {
        NodeLayoutData nodeLayoutData = NodeLayoutData.get(historyNode);
        Node nodeModel = historyNode.getNode();
        nodeModel.setGraphConstraints(nodeLayoutData.getX(), nodeLayoutData.getY(), nodeLayoutData.getWidth(), nodeLayoutData.getHeight());
        return nodeModel;
    }

    /**
     * Returns {@link NodeType} for node. EXCLUSIVE_GATEWAY is replaced with DECISION and PARALLEL_GATEWAY replaced with FORK or JOIN
     *
     * @param nodeId
     *            Node id.
     * @return
     */
    NodeType getNodeType(HistoryGraphNode historyGraphNode) {
        Node node = historyGraphNode.getNode();
        switch (node.getNodeType()) {
        case EXCLUSIVE_GATEWAY:
            return NodeType.DECISION;
        case PARALLEL_GATEWAY:
            return node.getLeavingTransitions().size() > 1 ? NodeType.FORK : NodeType.JOIN;
        default:
            return node.getNodeType();
        }
    }

    private void createFigureForNode(HistoryGraphNode node, FiguresNodeData data, Node model) {
        model.setGraphMinimizedView(false);
        AbstractFigure nodeFigure = factory.createFigure(model, false);
        boolean hasOutTransition = !node.getTransitions().isEmpty();
        boolean isFinalNode = model.getNodeType() == NodeType.END_TOKEN || model.getNodeType() == NodeType.END_PROCESS;
        Color color = hasOutTransition || isFinalNode ? DrawProperties.getHighlightColor() : DrawProperties.getBaseColor();
        RenderHits renderHits = new RenderHits(color, hasOutTransition, !hasOutTransition);
        nodeFigure.setRenderHits(renderHits);
        // nodeFigure.setType(getNodeType(node));
        data.setFigure(nodeFigure);
    }

    private void createFigureForTransitions(HistoryGraphNode node, FiguresNodeData data) {
        try {
            for (HistoryGraphTransitionModel transition : node.getTransitions()) {
                createTransitionFigure(transition, data);
            }
        } catch (CloneNotSupportedException e) {
            throw new InternalApplicationException("Clone graph element error", e);
        }
    }

    private void createTransitionFigure(HistoryGraphTransitionModel transition, FiguresNodeData data) throws CloneNotSupportedException {
        FiguresNodeData fromNodeFigure = FiguresNodeData.getOrThrow(transition.getFromNode());
        FiguresNodeData toNodeFigure = FiguresNodeData.getOrThrow(transition.getToNode());
        Transition wfeTransition = transition.getTransition().clone();
        wfeTransition.setName(CalendarUtil.formatDateTime(transition.getLog().getCreateDate()));
        wfeTransition.getBendpoints().clear();
        if (getNodeType(transition.getFromNode()) == NodeType.FORK) {
            int x = toNodeFigure.getFigure().getCoords()[0] + toNodeFigure.getFigure().getCoords()[2] / 2;
            int y = fromNodeFigure.getFigure().getGraphY() + fromNodeFigure.getFigure().getGraphHeight() / 2;
            wfeTransition.getBendpoints().add(new Bendpoint(x, y));
        }
        if (getNodeType(transition.getFromNode()) == NodeType.JOIN) {
            int x = fromNodeFigure.getFigure().getGraphX() + fromNodeFigure.getFigure().getGraphWidth() / 2;
            int y = toNodeFigure.getFigure().getCoords()[1];
            wfeTransition.getBendpoints().add(new Bendpoint(x, y));
        }

        TransitionFigure figure = factory.createTransitionFigure();
        figure.init(wfeTransition, fromNodeFigure.getFigure(), toNodeFigure.getFigure(), false);
        figure.setRenderHits(new RenderHits(DrawProperties.getTransitionColor()));
        if (wfeTransition.isTimerTransition()) {
            figure.setTimerInfo(getTimerInfo(transition.getFromNode().getNode()));
        }
        data.addTransition(figure);
    }

    static String getTimerInfo(Node node) {
        try {
            List<CreateTimerAction> actions = CreateTimerAction.getNodeTimerActions(node, false);
            if (actions.size() == 0) {
                return "No timer";
            }
            return actions.get(0).getDueDate();
        } catch (Exception e) {
            return e.getClass().getName();
        }
    }
}
