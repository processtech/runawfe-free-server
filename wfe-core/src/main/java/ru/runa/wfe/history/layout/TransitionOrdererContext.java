package ru.runa.wfe.history.layout;

import ru.runa.wfe.history.graph.HistoryGraphParallelNodeModel;

/**
 * Context for transition reordering operation.
 */
public class TransitionOrdererContext {
    /**
     * Flag, equals true, if parallel node must be reordered and false if no
     * reordering require - just store find node.
     */
    private final boolean reordering;
    /**
     * Node, which will find in graph. May be null.
     */
    private HistoryGraphParallelNodeModel findNode;

    /**
     * Creates context for transition reordering.
     */
    public TransitionOrdererContext(boolean reordering) {
        this.reordering = reordering;
    }

    /**
     * Returns node, followed by current.
     */
    public HistoryGraphParallelNodeModel getFindNode() {
        return findNode;
    }

    /**
     * Set node, followed by current.
     */
    public void setFindNode(HistoryGraphParallelNodeModel findNode) {
        this.findNode = findNode;
    }

    /**
     * Flag, equals true, if parallel node must be reordered and false if no
     * reordering require - just store find node.
     */
    public boolean isReordering() {
        return reordering;
    }
}
