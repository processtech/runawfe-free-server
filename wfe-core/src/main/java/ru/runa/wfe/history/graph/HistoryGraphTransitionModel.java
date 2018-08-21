package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.ITransitionLog;
import ru.runa.wfe.lang.Transition;

/**
 * History graph model for transition between nodes.
 */
public class HistoryGraphTransitionModel {

    /**
     * History graph node, from which transition leave.
     */
    private final HistoryGraphNode fromNode;
    /**
     * History graph node, which accept transition.
     */
    private final HistoryGraphNode toNode;
    /**
     * Log instance for current transition.
     */
    private final ITransitionLog log;
    /**
     * WFE transition model.
     */
    private final Transition transition;

    public HistoryGraphTransitionModel(HistoryGraphNode fromNode, HistoryGraphNode toNode, ITransitionLog log) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.log = log;
        this.transition = getTransitionModel(fromNode, log);
    }

    /**
     * Search transition model for transition log entry.
     * 
     * @param node
     *            History graph node, from which transition moved up.
     * @param log
     *            Transition log entry.
     * @return Returns WFE transition model.
     */
    private Transition getTransitionModel(HistoryGraphNode node, ITransitionLog log) {
        for (Transition transition : node.getNode().getLeavingTransitions()) {
            if (transition.getNodeId().equals(log.getNodeId())) {
                return transition;
            }
        }
        return null;
    }

    /**
     * Get history graph node, which accept transition.
     * 
     * @return Returns history graph node, which accept transition.
     */
    public HistoryGraphNode getToNode() {
        return toNode;
    }

    /**
     * Get history graph node, from which transition leave.
     * 
     * @return Returns history graph node, from which transition leave.
     */
    public HistoryGraphNode getFromNode() {
        return fromNode;
    }

    /**
     * Get node id for transition log instance.
     * 
     * @return Returns node id for transition log instance.
     */
    public String getNodeId() {
        return log.getNodeId();
    }

    /**
     * Get transition log instance.
     * 
     * @return Returns transition log instance.
     */
    public ITransitionLog getLog() {
        return log;
    }

    /**
     * Get WFE transition model.
     * 
     * @return Returns WFE transition model.
     */
    public Transition getTransition() {
        return transition;
    }
}
