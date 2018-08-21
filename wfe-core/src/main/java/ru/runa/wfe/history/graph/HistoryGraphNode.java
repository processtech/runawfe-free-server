package ru.runa.wfe.history.graph;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.lang.Node;

/**
 * Interface for all nodes in history graph.
 */
public interface HistoryGraphNode {
    /**
     * Node leaving transitions.
     * 
     * @return Returns node leaving transitions.
     */
    List<HistoryGraphTransitionModel> getTransitions();

    /**
     * Node incoming transitions.
     * 
     * @return Returns node incoming transitions.
     */
    List<HistoryGraphTransitionModel> getIncomingTransitions();

    /**
     * Get node model from process definition.
     * 
     * @return Returns node model.
     */
    Node getNode();

    /**
     * Node id from process definition, which correspond to this node.
     * 
     * @return Return node id.
     */
    String getNodeId();

    /**
     * Custom data, stored in node.
     * 
     * @return Returns all custom data, available on node.
     */
    Map<String, Object> getCustomData();

    /**
     * Returns first instance of of log with given type.
     * 
     * @return Returns first instance of of log with given type.
     */
    <T extends IProcessLog> T getNodeLog(IProcessLog.Type type);

    /**
     * Returns all instances of of log with given type.
     * 
     * @return Returns all instances of of log with given type.
     */
    <T extends IProcessLog> List<T> getNodeLogs(IProcessLog.Type type);

    /**
     * Check if node may accept log instance.
     * 
     * @param nodeId
     *            Node id of log instance.
     * @return Returns true, if it seems, what log instance belongs to this node
     *         and false otherwise.
     */
    boolean mayAcceptLog(String nodeId);

    /**
     * Store log instance in node and creates transition to other node if
     * required.
     *
     * @param log
     *            Log instance to accept.
     * @return Returns node, created for transition or null, if no node created.
     */
    HistoryGraphNode acceptLog(IProcessLog log);

    /**
     * Check if node can accept leaving transition. For example generic node may
     * accept only 1 transition, but fork node may accept many transitions.
     * 
     * @return Returns true, if node may accept additional leaving transition
     *         and false otherwise.
     */
    boolean mayAcceptNewTransition();

    /**
     * Applies operation to node.
     * 
     * @param visitor
     *            Operation to be applied to graph node.
     * @param context
     *            Operation context (will be passed to operation).
     */
    <TContext> void processBy(HistoryGraphNodeVisitor<TContext> visitor, TContext context);

    /**
     * Add incoming transition to the node.
     * 
     * @param transition
     *            Incoming transition model
     */
    void registerIncomingTransition(HistoryGraphTransitionModel transition);
}
