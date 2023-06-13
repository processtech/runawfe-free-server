package ru.runa.wfe.history.graph;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;

/**
 * Factory for history graph nodes creation.
 */
public class HistoryGraphNodeFactoryImpl implements HistoryGraphNodeFactory {

    /**
     * Maps from node id to history graph nodes, which currently may accept
     * incoming transitions. Used to get existing nodes for join and parallel
     * gateways.
     */
    private final Map<String, List<HistoryGraphNode>> currentWorkNodes;

    public HistoryGraphNodeFactoryImpl(Map<String, List<HistoryGraphNode>> currentWorkNodes) {
        super();
        this.currentWorkNodes = currentWorkNodes;
    }

    @Override
    public HistoryGraphNode createNodeModel(TransitionLog log, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        switch (node.getNodeType()) {
        case FORK:
            return new HistoryGraphForkNodeModel(node, definitionModel, nodeFactory);
        case JOIN: {
            List<HistoryGraphNode> list = currentWorkNodes.get(node.getNodeId());
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
            return new HistoryGraphJoinNodeModel(node, definitionModel, nodeFactory);
        }
        case PARALLEL_GATEWAY: {
            List<HistoryGraphNode> list = currentWorkNodes.get(node.getNodeId());
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
            return new HistoryGraphParallelNodeModel(node, definitionModel, nodeFactory);
        }
        default:
            return new HistoryGraphGenericNodeModel(node, definitionModel, nodeFactory);
        }
    }
}
