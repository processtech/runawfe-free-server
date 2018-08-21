package ru.runa.wfe.history.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;

/**
 * Base model for history graph node.
 */
public abstract class HistoryGraphBaseNodeModel implements HistoryGraphNode {
    /**
     * Process instance node definition.
     */
    private final Node node;
    /**
     * Leaving transitions from this node.
     */
    private final List<HistoryGraphTransitionModel> transitions = Lists.newArrayList();
    /**
     * Incoming transitions from this node.
     */
    private final List<HistoryGraphTransitionModel> incomingTransitions = Lists.newArrayList();
    /**
     * Logs, belongs to this node.
     */
    private final List<IProcessLog> nodeLogs = Lists.newArrayList();
    /**
     * Process instance data.
     */
    private final ProcessInstanceData definitionModel;
    /**
     * Factory to create history graph nodes.
     */
    private final HistoryGraphNodeFactory nodeFactory;
    /**
     * Custom data, attached to this node.
     */
    private final Map<String, Object> customData = Maps.newHashMap();

    /**
     * Creates node from log instance.
     * 
     * @param processLog
     *            Log instance.
     * @param definitionModel
     *            Data for process instance and definition.
     * @param nodeFactory
     *            Factory to create nodes.
     */
    protected HistoryGraphBaseNodeModel(IProcessLog processLog, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        try {
            this.definitionModel = definitionModel;
            this.nodeFactory = nodeFactory;
            node = definitionModel.getNode(processLog.getNodeId()).clone();
            node.setGraphMinimizedView(false);
        } catch (CloneNotSupportedException e) {
            throw new InternalApplicationException("Failed to clone node for graph history", e);
        }
    }

    /**
     * Creates node from log instance for specific {@link Node}.
     * 
     * @param processLog
     *            Log instance.
     * @param node
     *            Node instance to create history graph node.
     * @param definitionModel
     *            Data for process instance and definition.
     * @param nodeFactory
     *            Factory to create nodes.
     */
    protected HistoryGraphBaseNodeModel(IProcessLog processLog, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory) {
        try {
            this.definitionModel = definitionModel;
            this.nodeFactory = nodeFactory;
            this.node = node.clone();
            this.node.setGraphMinimizedView(false);
        } catch (CloneNotSupportedException e) {
            throw new InternalApplicationException("Failed to clone node for graph history", e);
        }
    }

    @Override
    public boolean mayAcceptLog(String nodeId) {
        return nodeId.equals(getNode().getNodeId());
    }

    @Override
    public HistoryGraphNode acceptLog(IProcessLog log) {
        nodeLogs.add(log);
        if (!(log instanceof TransitionLog)) {
            return null;
        }
        if (!mayAcceptNewTransition()) {
            String message = "Unexpected leaving transition number " + (transitions.size() + 1) + " for node with id " + getNode().getNodeId();
            throw new InternalApplicationException(message);
        }
        return addTransition((TransitionLog) log);
    }

    @Override
    public <T extends IProcessLog> T getNodeLog(IProcessLog.Type type) {
        for (IProcessLog log : nodeLogs) {
            if (log.getType() == type) {
                return (T) log;
            }
        }
        return null;
    }

    @Override
    public <T extends IProcessLog> List<T> getNodeLogs(IProcessLog.Type type) {
        val result = new ArrayList<T>();
        for (IProcessLog log : nodeLogs) {
            if (log.getType() == type) {
                result.add((T) log);
            }
        }
        return result;
    }

    /**
     * Add leaving transition from current node.
     * 
     * @param log
     *            {@link TransitionLog} for transition creation.
     * @return Return's created graph history node (transition to).
     */
    private HistoryGraphNode addTransition(TransitionLog log) {
        HistoryGraphNode newNode = nodeFactory.createNodeModel(log, definitionModel.getNode(log.getToNodeId()), definitionModel, nodeFactory);
        HistoryGraphTransitionModel transitionModel = new HistoryGraphTransitionModel(this, newNode, log);
        transitions.add(transitionModel);
        newNode.registerIncomingTransition(transitionModel);
        return newNode;
    }

    @Override
    public void registerIncomingTransition(HistoryGraphTransitionModel transition) {
        incomingTransitions.add(transition);
    }

    /**
     * Reorders transitions with specified order.
     * 
     * @param newOrder
     *            New transition order.
     */
    public void reorderTransitions(List<Integer> newOrder) {
        List<HistoryGraphTransitionModel> current = new ArrayList<HistoryGraphTransitionModel>(getTransitions());
        for (int i = 0; i < newOrder.size(); ++i) {
            transitions.set(i, current.get(newOrder.get(i)));
        }
    }

    @Override
    public String getNodeId() {
        return getNode().getNodeId();
    }

    @Override
    public List<HistoryGraphTransitionModel> getTransitions() {
        return transitions;
    }

    @Override
    public List<HistoryGraphTransitionModel> getIncomingTransitions() {
        return incomingTransitions;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public Map<String, Object> getCustomData() {
        return customData;
    }
}
