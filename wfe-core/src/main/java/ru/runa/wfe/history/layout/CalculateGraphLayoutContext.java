package ru.runa.wfe.history.layout;

import java.util.Stack;

import ru.runa.wfe.history.graph.HistoryGraphForkNodeModel;

/**
 * Context of graph layout computation operation.
 */
public class CalculateGraphLayoutContext {
    /**
     * Current graph flow offset from 0. For example every fork node advice
     * offset for next transition on width of previous transition.
     */
    private final int widthOffset;
    /**
     * Stack for Fork/Join elements subtree offset. This offset used, when
     * Fork/Join elements width is higher when sum of leaving trees width.
     * 
     * Used to center elements inside Fork/Join.
     */
    private final Stack<SubtreeOffset> subtreeOffset;
    /**
     * Full tree height. Used to calculate elements y position.
     */
    private final int fullTreeHeight;

    public CalculateGraphLayoutContext(int fullTreeHeight) {
        super();
        this.widthOffset = 0;
        this.fullTreeHeight = fullTreeHeight;
        subtreeOffset = new Stack<SubtreeOffset>();
    }

    private CalculateGraphLayoutContext(int widthOffset, int fullTreeHeight, Stack<SubtreeOffset> subtreeOffset) {
        this.widthOffset = widthOffset;
        this.fullTreeHeight = fullTreeHeight;
        this.subtreeOffset = subtreeOffset;
    }

    public void calculateNodePosition(NodeLayoutData nodeLayoutData) {
        int xOffset = widthOffset + getSubtreeOffset();
        nodeLayoutData.setX(xOffset + (nodeLayoutData.getSubtreeWidth() - nodeLayoutData.getPreferredWidth()) / 2);
        nodeLayoutData.setY(fullTreeHeight - nodeLayoutData.getSubtreeHeight());
        nodeLayoutData.setWidth(nodeLayoutData.getPreferredWidth() > 0 ? nodeLayoutData.getPreferredWidth() : nodeLayoutData.getWidth());
        nodeLayoutData.setHeight(nodeLayoutData.getHeight() > HistoryGraphLayoutProperties.maxNodeHeight ? HistoryGraphLayoutProperties.maxNodeHeight
                : nodeLayoutData.getHeight());
    }

    /**
     * Get current width offset.
     * 
     * @return Returns current width offset.
     */
    int getWidth() {
        return widthOffset;
    }

    /**
     * Get current subtree offset.
     * 
     * @return Returns current subtree offset.
     */
    int getSubtreeOffset() {
        if (!subtreeOffset.isEmpty()) {
            return subtreeOffset.peek().offset;
        }
        return 0;
    }

    /**
     * Set current subtree offset.
     * 
     * @param node
     *            Node, which set offset.
     * @param offset
     *            Offset value.
     */
    void setSubtreeOffset(HistoryGraphForkNodeModel node, int offset) {
        subtreeOffset.push(new SubtreeOffset(node, offset));
    }

    /**
     * Reset current subtree offset and return's it value.
     * 
     * @return Return's reseted offset value.
     */
    SubtreeOffset resetSubtreeOffset() {
        return subtreeOffset.pop();
    }

    /**
     * Removes subtree offset for specified node if it exists on stack.
     * 
     * @param node
     *            Node, which offset need to be removed.
     */
    public void resetSubtreeOffset(HistoryGraphForkNodeModel node) {
        if (subtreeOffset.isEmpty()) {
            return;
        }
        if (subtreeOffset.peek().initiatedNode == node) {
            subtreeOffset.pop();
        }
    }

    /**
     * Creates new context with given offset.
     * 
     * @param widthOffset
     *            Offset for layout calculation.
     * @return Returns new layout computation context.
     */
    public CalculateGraphLayoutContext createNew(int widthOffset) {
        return new CalculateGraphLayoutContext(widthOffset, fullTreeHeight, subtreeOffset);
    }

    /**
     * Stores offset, saved by Fork Node.
     */
    public static class SubtreeOffset {
        /**
         * Node, which generates offset.
         */
        public final HistoryGraphForkNodeModel initiatedNode;
        /**
         * Offset value (x coordinate).
         */
        public final int offset;

        public SubtreeOffset(HistoryGraphForkNodeModel initiatedNode, int offset) {
            this.initiatedNode = initiatedNode;
            this.offset = offset;
        }
    }
}
