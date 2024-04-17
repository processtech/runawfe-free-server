package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Area;
import org.apache.ecs.html.Map;
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.graph.view.MultiSubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementVisitor;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.TaskNodeGraphElement;

/**
 * Operation to create tool tips on and links on process history graph.
 */
public class GraphHistoryElementPresentationVisitor extends NodeGraphElementVisitor {
    /**
     * Helper to create html.
     */
    private final GraphElementPresentationHelper presentationHelper;
    private final TD td;

    /**
     * Creates operation to add tool tips and links on process history graph.
     *
     * @param taskId
     *            Current task identity.
     * @param pageContext
     *            Rendered page context.
     * @param tdFormElement
     *            Main TD element of form, containing graph history.
     */
    public GraphHistoryElementPresentationVisitor(PageContext pageContext, TD td, String subprocessId) {
        this.td = td;
        presentationHelper = new GraphElementPresentationHelper(pageContext, subprocessId);
    }

    @Override
    protected void onMultiSubprocessNode(MultiSubprocessNodeGraphElement element) {
        td.addElement(presentationHelper.createMultiSubprocessLinks(element, WebResources.ACTION_SHOW_GRAPH_HISTORY));
        addTooltip(element, null);
    }

    @Override
    protected void onSubprocessNode(SubprocessNodeGraphElement element) {
        Area area = presentationHelper.createSubprocessLink(element, WebResources.ACTION_SHOW_GRAPH_HISTORY,
                "javascript:showEmbeddedSubprocessGraphHistory", false, true);
        addTooltip(element, area);
    }

    @Override
    protected void onTaskNode(TaskNodeGraphElement element) {
        Area area = presentationHelper.createTaskTooltip(element);
        addTooltip(element, area);
    }

    /**
     * Operation result.
     *
     * @return Map of elements, represents links and tool tips areas.
     */
    public Map getResultMap() {
        return presentationHelper.getMap();
    }

    private Area addTooltip(NodeGraphElement element, Area area) {
        return presentationHelper.addTooltip(element, area, element.getLabel());
    }

}
