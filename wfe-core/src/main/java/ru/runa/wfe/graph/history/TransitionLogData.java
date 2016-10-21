package ru.runa.wfe.graph.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.runa.wfe.audit.NodeLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.TransitionLog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Passed transitions data.
 */
public class TransitionLogData {
    /**
     * Map from 'FROM' node name to transitions, passed from node.
     */
    private final HashMap<String, ArrayList<TransitionLog>> fromNodeToTransition = Maps.newHashMap();
    /**
     * Map from 'TO' node name to transitions, passed to node.
     */
    private final HashMap<String, ArrayList<TransitionLog>> toNodeToTransition = Maps.newHashMap();
    /**
     * All transition logs.
     */
    private final List<TransitionLog> transitionLogs = Lists.newArrayList();

    public TransitionLogData(List<ProcessLog> processLogs) {
        super();
        for (ProcessLog log : processLogs) {
            if (!(log instanceof TransitionLog)) {
                continue;
            }
            TransitionLog transitionLog = (TransitionLog) log;
            getTransitionLogs().add(transitionLog);
            addToArrayMap(fromNodeToTransition, transitionLog.getFromNodeId(), transitionLog);
            addToArrayMap(toNodeToTransition, transitionLog.getToNodeId(), transitionLog);
        }
    }

    /**
     * Adds transition log to array inside map by node id.
     * 
     * @param nodeToTransition
     *            Map from node id to array of transitions.
     * @param nodeId
     *            Node id to search array in the map.
     * @param transitionLog
     *            Transition log to add to the map.
     */
    private void addToArrayMap(HashMap<String, ArrayList<TransitionLog>> nodeToTransition, String nodeId, TransitionLog transitionLog) {
        ArrayList<TransitionLog> logs = nodeToTransition.get(nodeId);
        if (logs == null) {
            logs = new ArrayList<TransitionLog>();
            nodeToTransition.put(nodeId, logs);
        }
        logs.add(transitionLog);
    }

    public List<TransitionLog> getTransitionLogs() {
        return transitionLogs;
    }

    public ArrayList<TransitionLog> getTransitionLogsFromNode(String nodeId) {
        return fromNodeToTransition.get(nodeId);
    }

    public TransitionLog findNextTransitionLog(NodeLog log, String nodeId) {
        ArrayList<TransitionLog> transitionLogs = fromNodeToTransition.get(nodeId);
        if (transitionLogs == null) {
            return null;
        }
        for (TransitionLog tempLog : transitionLogs) {
            if (tempLog.getId() > log.getId()) {
                return tempLog;
            }
        }
        return null;
    }
}
