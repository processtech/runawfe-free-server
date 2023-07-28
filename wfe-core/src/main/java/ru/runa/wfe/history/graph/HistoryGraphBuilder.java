package ru.runa.wfe.history.graph;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;

/**
 * Parse logs and build node graph.
 */
public final class HistoryGraphBuilder {

    /**
     * Parse logs and build node graph.
     * 
     * @param logs
     *            Process instance logs to parse.
     * @param definitionModel
     *            Process instance and definition data.
     * @return Returns root node of history graph.
     */
    public static HistoryGraphNode buildHistoryGraph(List<BaseProcessLog> logs, ProcessInstanceData definitionModel) {
        Map<String, List<HistoryGraphNode>> currentWorkNodes = Maps.newHashMap();
        HistoryGraphNode root = new HistoryGraphGenericNodeModel(logs.get(0), definitionModel, new HistoryGraphNodeFactoryImpl(currentWorkNodes));
        ArrayList<HistoryGraphNode> arrayList = new ArrayList<>();
        arrayList.add(root);
        currentWorkNodes.put(logs.get(0).getNodeId(), arrayList);
        for (BaseProcessLog log : logs) {
            if (log.getNodeId() == null) {
                continue;
            }
            HistoryGraphNode logNodeModel = getLogNodeModel(log, currentWorkNodes, definitionModel);
            if (logNodeModel == null) {
                continue;
            }
            HistoryGraphNode childNodeModel = logNodeModel.acceptLog(log);
            if (childNodeModel != null) {
                List<HistoryGraphNode> nodes = currentWorkNodes.get(childNodeModel.getNodeId());
                if (nodes == null) {
                    nodes = new ArrayList<>();
                    currentWorkNodes.put(childNodeModel.getNodeId(), nodes);
                }
                if (!nodes.contains(childNodeModel)) {
                    nodes.add(childNodeModel);
                }
                if (!logNodeModel.mayAcceptNewTransition()) {
                    currentWorkNodes.get(logNodeModel.getNodeId()).remove(logNodeModel);
                }
            }
        }
        return root;
    }

    /**
     * Get graph node model for process log.
     * 
     * @param log
     *            Process log instance.
     * @param currentWorkNodes
     *            Nodes, which currently may generate logs.
     * @return
     */
    private static HistoryGraphNode getLogNodeModel(BaseProcessLog log, Map<String, List<HistoryGraphNode>> currentWorkNodes,
            ProcessInstanceData definitionModel) {
        String nodeId = log.getNodeId();
        if (log instanceof TransitionLog) {
            nodeId = ((TransitionLog) log).getFromNodeId();
        }
        List<HistoryGraphNode> nodes = currentWorkNodes.get(nodeId);
        if (nodes == null) {
            return null;
            // throw new
            // InternalApplicationException("Graph history build error: failed to find node with id "
            // + nodeId);
        }
        for (HistoryGraphNode node : nodes) {
            if (node.mayAcceptLog(nodeId)) {
                return node;
            }
        }
        return null;
        // String message =
        // "Graph history build error: failed to find node with id " + nodeId +
        // " and tokenId " + log.getTokenId();
        // throw new InternalApplicationException(message);
    }
}
