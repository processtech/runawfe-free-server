package ru.runa.wfe.history.graph;

/**
 * Interface for operation, which may be applied to history graph node.
 * 
 * @param <TContext>
 *            Operation context type.
 */
public interface HistoryGraphNodeVisitor<TContext> {

    /**
     * Called to apply operation for fork node.
     * 
     * @param node
     *            Fork node model to apply operation.
     * @param context
     *            Operation context.
     */
    void onForkNode(HistoryGraphForkNodeModel node, TContext context);

    /**
     * Called to apply operation for join node.
     * 
     * @param node
     *            Join node model to apply operation.
     * @param context
     *            Operation context.
     */
    void onJoinNode(HistoryGraphJoinNodeModel node, TContext context);

    /**
     * Called to apply operation for parallel node.
     * 
     * @param node
     *            Parallel node model to apply operation.
     * @param context
     *            Operation context.
     */
    void onParallelNode(HistoryGraphParallelNodeModel node, TContext context);

    /**
     * Called to apply operation for generic node.
     * 
     * @param node
     *            Generic node model to apply operation.
     * @param context
     *            Operation context.
     */
    void onGenericNode(HistoryGraphGenericNodeModel node, TContext context);
}
