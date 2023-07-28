package ru.runa.wfe.graph.view;

import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.SubprocessNode;

/**
 * Represents Subprocess element on process graph.
 */
public class SubprocessNodeGraphElement extends NodeGraphElement {

    private static final long serialVersionUID = 1L;

    /**
     * Forked subprocess identity.
     */
    private Long subprocessId;

    /**
     * Flag, equals true, if subprocess is accessible by current user; false
     * otherwise.
     */
    private boolean subprocessAccessible;

    /**
     * Name of subprocess.
     */
    private String subprocessName;
    private boolean embedded;
    private String embeddedSubprocessId;
    private int embeddedSubprocessGraphWidth;
    private int embeddedSubprocessGraphHeight;
    private boolean triggeredByEvent;

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        SubprocessNode subprocessNode = (SubprocessNode) node;
        this.subprocessName = subprocessNode.getSubProcessName();
        this.embedded = subprocessNode.isEmbedded();
        this.triggeredByEvent = subprocessNode.isTriggeredByEvent();
    }

    /**
     * Forked subprocess identity.
     */
    public Long getSubprocessId() {
        return subprocessId;
    }

    /**
     * Set forked subprocess identity.
     */
    public void setSubprocessId(Long subprocessId) {
        this.subprocessId = subprocessId;
    }

    /**
     * Flag, equals true, if subprocess is accessible by current user; false
     * otherwise.
     */
    public boolean isSubprocessAccessible() {
        return subprocessAccessible;
    }

    /**
     * Set flag, equals true, if subprocess is readable by current user; false
     * otherwise.
     */
    public void setSubprocessAccessible(boolean subprocessAccessible) {
        this.subprocessAccessible = subprocessAccessible;
    }

    /**
     * Name of subprocess.
     */
    public String getSubprocessName() {
        return subprocessName;
    }
    
    public boolean isEmbedded() {
        return embedded;
    }
    
    public String getEmbeddedSubprocessId() {
        return embeddedSubprocessId;
    }
    
    public void setEmbeddedSubprocessId(String embeddedSubprocessId) {
        this.embeddedSubprocessId = embeddedSubprocessId;
    }

    public int getEmbeddedSubprocessGraphWidth() {
        return embeddedSubprocessGraphWidth;
    }

    public void setEmbeddedSubprocessGraphWidth(int embeddedSubprocessGraphWidth) {
        this.embeddedSubprocessGraphWidth = embeddedSubprocessGraphWidth;
    }

    public int getEmbeddedSubprocessGraphHeight() {
        return embeddedSubprocessGraphHeight;
    }

    public void setEmbeddedSubprocessGraphHeight(int embeddedSubprocessGraphHeight) {
        this.embeddedSubprocessGraphHeight = embeddedSubprocessGraphHeight;
    }

    public boolean isTriggeredByEvent() {
        return triggeredByEvent;
    }

}
