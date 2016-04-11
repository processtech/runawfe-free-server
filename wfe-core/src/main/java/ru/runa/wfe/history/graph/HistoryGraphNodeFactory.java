package ru.runa.wfe.history.graph;

import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.graph.history.ProcessInstanceData;
import ru.runa.wfe.lang.Node;

/**
 * Factory to creates history graph nodes.
 */
public interface HistoryGraphNodeFactory {
    /**
     * Creates history graph node for specified {@link TransitionLog}. May
     * return existing node in case of join or parallel gateway.
     * 
     * @param log
     *            Transition log. Doesn't used to get node, just saved in new
     *            nodes.
     * @param node
     *            Node to create new history graph node.
     * @param definitionModel
     *            Process instance and definition data.
     * @param nodeFactory
     *            Factory to create history graph nodes.
     * @return Returns created (or existing) history graph node.
     */
    public HistoryGraphNode createNodeModel(TransitionLog log, Node node, ProcessInstanceData definitionModel, HistoryGraphNodeFactory nodeFactory);
}
