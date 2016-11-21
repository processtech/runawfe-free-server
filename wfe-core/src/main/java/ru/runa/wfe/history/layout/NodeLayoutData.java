package ru.runa.wfe.history.layout;

import ru.runa.wfe.history.graph.HistoryGraphNode;

/**
 * Custom data model to store layout data in history graph node.
 */
public class NodeLayoutData {

    /**
     * Key to load data from history graph node.
     */
    private static final String DATA_KEY = "LAYOUT_DATA";

    /**
     * Current node subtree width.
     */
    private int subtreeWidth = 0;
    /**
     * Current node subtree height.
     */
    private int subtreeHeight = 0;
    /**
     * Current node X position.
     */
    private int xPos = 0;
    /**
     * Current node Y position.
     */
    private int yPos = 0;
    /**
     * Node width.
     */
    private int width = 0;
    /**
     * Node height.
     */
    private int height = 0;
    /**
     * Preferred width for this node.
     */
    private int preferredWidth = 0;
    /**
     * Flag, equals true, if subtree width and height for this node is already
     * calculated; false otherwise.
     */
    private boolean subtreeWidthHeightCalulated = false;
    /**
     * Flag, equals true, if position for this node is already calculated; false
     * otherwise.
     */
    private boolean positionCalculated = false;

    /**
     * Flag, equals true, if subtree width and height for this node is already
     * calculated; false otherwise.
     */
    public boolean subtreeCalulationRequired() {
        boolean calculated = subtreeWidthHeightCalulated;
        subtreeWidthHeightCalulated = true;
        return !calculated;
    }

    /**
     * Flag, equals true, if position for this node is already calculated; false
     * otherwise.
     */
    public boolean positionCalulationRequired() {
        boolean calculated = positionCalculated;
        positionCalculated = true;
        return !calculated;
    }

    /**
     * Get subtree width.
     */
    public int getSubtreeWidth() {
        return subtreeWidth;
    }

    /**
     * Set subtree width.
     */
    public void setSubtreeWidth(int subtreeWidth) {
        this.subtreeWidth = subtreeWidth;
    }

    /**
     * Get subtree height.
     */
    public int getSubtreeHeight() {
        return subtreeHeight;
    }

    /**
     * Set subtree height.
     */
    public void setSubtreeHeight(int subtreeHeight) {
        this.subtreeHeight = subtreeHeight;
    }

    /**
     * Get X coordinates.
     */
    public int getX() {
        return xPos;
    }

    /**
     * Set X coordinates.
     */
    public void setX(int xPos) {
        this.xPos = xPos;
    }

    /**
     * Get Y coordinates.
     */
    public int getY() {
        return yPos;
    }

    /**
     * Set Y coordinates.
     */
    public void setY(int yPos) {
        this.yPos = yPos;
    }

    /**
     * Get node width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set node width.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get node height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set node height.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get preferred width.
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    /**
     * Set preferred width.
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    /**
     * Returns array with graph element constraints. <i>xPos, yPos, xPos +
     * width, yPos + height</i>
     */
    public int[] getConstraints() {
        return new int[] { xPos, yPos, xPos + width, yPos + height };
    }

    /**
     * Read layout data from node and returns it. If no layout data specified
     * for node then create and save new layout data for node.
     * 
     * @param node
     *            Node to get layout data.
     * @return Returns layout data for node. Always not null.
     */
    public static NodeLayoutData get(HistoryGraphNode node) {
        NodeLayoutData data = (NodeLayoutData) node.getCustomData().get(DATA_KEY);
        if (data == null) {
            data = new NodeLayoutData();
            node.getCustomData().put(DATA_KEY, data);
        }
        return data;
    }
}
