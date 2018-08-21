package ru.runa.wfe.graph.history;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.INodeLog;
import ru.runa.wfe.audit.ITransitionLog;

/**
 * Passed transitions data.
 */
public class TransitionLogData {
    /**
     * Map from 'FROM' node name to transitions, passed from node.
     */
    private final HashMap<String, ArrayList<ITransitionLog>> fromNodeToTransition = Maps.newHashMap();
    /**
     * Map from 'TO' node name to transitions, passed to node.
     */
    private final HashMap<String, ArrayList<ITransitionLog>> toNodeToTransition = Maps.newHashMap();
    /**
     * All transition logs.
     */
    private final List<ITransitionLog> transitionLogs = Lists.newArrayList();

    public TransitionLogData(List<? extends BaseProcessLog> processLogs) {
        super();
        for (BaseProcessLog log : processLogs) {
            if (!(log instanceof ITransitionLog)) {
                continue;
            }
            ITransitionLog transitionLog = (ITransitionLog) log;
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
    private void addToArrayMap(HashMap<String, ArrayList<ITransitionLog>> nodeToTransition, String nodeId, ITransitionLog transitionLog) {
        ArrayList<ITransitionLog> logs = nodeToTransition.get(nodeId);
        if (logs == null) {
            logs = new ArrayList<>();
            nodeToTransition.put(nodeId, logs);
        }
        logs.add(transitionLog);
    }

    public List<ITransitionLog> getTransitionLogs() {
        return transitionLogs;
    }

    public ArrayList<ITransitionLog> getTransitionLogsFromNode(String nodeId) {
        return fromNodeToTransition.get(nodeId);
    }

    public ITransitionLog findNextTransitionLog(INodeLog log, String nodeId) {
        ArrayList<ITransitionLog> transitionLogs = fromNodeToTransition.get(nodeId);
        if (transitionLogs == null) {
            return null;
        }
        for (ITransitionLog tempLog : transitionLogs) {
            if (tempLog.getId() > log.getId()) {
                return tempLog;
            }
        }
        return null;
    }
}
