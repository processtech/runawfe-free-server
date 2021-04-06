package ru.runa.wfe.graph.view;

import java.util.List;

import ru.runa.wfe.lang.NodeType;

/**
 * Interface for operations, applied to {@link NodeGraphElement}.
 */
public abstract class NodeGraphElementVisitor {

    public void visit(List<NodeGraphElement> elements) {
        for (NodeGraphElement presentation : elements) {
            visit(presentation);
        }
    }

    /**
     * Handle any graph element here.
     *
     * @param element
     *            Element to handle.
     */
    public void visit(NodeGraphElement element) {
        if (element.getNodeType() == NodeType.SUBPROCESS) {
            onSubprocessNode((SubprocessNodeGraphElement) element);
        }
        if (element.getNodeType() == NodeType.MULTI_SUBPROCESS) {
            onMultiSubprocessNode((MultiSubprocessNodeGraphElement) element);
        }
        if (element.getNodeType() == NodeType.TASK_STATE || element.getNodeType() == NodeType.MULTI_TASK_STATE) {
            onTaskNode((TaskNodeGraphElement) element);
        }
    }

    /**
     * Handle multiple instance graph element here.
     *
     * @param element
     *            Element to handle.
     */
    protected void onMultiSubprocessNode(MultiSubprocessNodeGraphElement element) {

    }

    /**
     * Handle subprocesses graph element here.
     *
     * @param element
     *            Element to handle.
     */
    protected void onSubprocessNode(SubprocessNodeGraphElement element) {

    }

    /**
     * Handle task state graph element here.
     *
     * @param element
     *            Element to handle.
     */
    protected void onTaskNode(TaskNodeGraphElement element) {
    }

}
