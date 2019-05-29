package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.graph.view.NodeGraphElementVisitor;
import ru.runa.wfe.graph.view.MultiSubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.TaskNodeGraphElement;

/**
 * Operation to create links to subprocess definitions and tool tips for
 * minimized states.
 */
public class DefinitionNodeGraphElementVisitor extends NodeGraphElementVisitor {
    /**
     * Helper to create html
     */
    private final GraphElementPresentationHelper presentationHelper;

    public DefinitionNodeGraphElementVisitor(PageContext pageContext, String subprocessId) {
        presentationHelper = new GraphElementPresentationHelper(pageContext, subprocessId);
    }

    @Override
    protected void onSubprocessNode(SubprocessNodeGraphElement element) {
        presentationHelper.createSubprocessDefinitionLink(element);
    }

    @Override
    protected void onMultiSubprocessNode(MultiSubprocessNodeGraphElement element) {
        presentationHelper.createSubprocessDefinitionLink(element);
    }

    @Override
    protected void onTaskNode(TaskNodeGraphElement element) {
        presentationHelper.createTaskTooltip(element);
    }

    public GraphElementPresentationHelper getPresentationHelper() {
        return presentationHelper;
    }

}
