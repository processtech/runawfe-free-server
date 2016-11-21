package ru.runa.wfe.history.layout;

/**
 * History graph layout common properties.
 */
public class HistoryGraphLayoutProperties {

    /**
     * Node height + height between nodes. If node exceed this height, it will
     * overlaps with other nodes.
     */
    public static final int cellHeight = 150;

    /**
     * Maximum node height. If node exceed this height, it will overlaps with
     * other nodes.
     */
    public static final int maxNodeHeight = 130;

    /**
     * Width between 2 nodes.
     */
    public static final int widthBetweenNodes = 20;

    /**
     * Minimal node width.
     */
    public static final int minNodeWidth = 100;

    /**
     * Height for join element.
     */
    public static final int joinHeight = 4;
}
