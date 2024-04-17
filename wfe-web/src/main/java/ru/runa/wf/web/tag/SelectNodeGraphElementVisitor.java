package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementVisitor;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.lang.NodeType;

public class SelectNodeGraphElementVisitor extends NodeGraphElementVisitor {

    private final GraphElementPresentationHelper presentationHelper;

    public SelectNodeGraphElementVisitor(PageContext pageContext, TD td, String subprocessId) {
        presentationHelper = new GraphElementPresentationHelper(pageContext, subprocessId);
    }

    @Override
    public void visit(NodeGraphElement element) {
        Area area = null;
        if (element.getNodeType() == NodeType.SUBPROCESS) {
            SubprocessNodeGraphElement subprocessElement = (SubprocessNodeGraphElement)element;
            if (subprocessElement.isSubprocessAccessible() && subprocessElement.isEmbedded()) {
                String url = "javascript:showEmbeddedSubprocessInSelectMode(" + subprocessElement.getSubprocessId() + ", '" +
                        subprocessElement.getEmbeddedSubprocessId() + "', " + subprocessElement.getEmbeddedSubprocessGraphWidth() +
                        ", " + subprocessElement.getEmbeddedSubprocessGraphHeight() + ");";
                area = new Area("RECT", element.getGraphConstraints());
                area.setHref(url);
                area.setTitle(subprocessElement.getSubprocessName());
                presentationHelper.getMap().addElement(area);
            }
        }
        if (area == null) {
            presentationHelper.createSelectElementLink(element);
        } else {
            area.setHref(area.getAttribute("href") + "javascript:selectProcessNode('" + element.getNodeId() + "', false);");
        }
    }

    public GraphElementPresentationHelper getPresentationHelper() {
        return presentationHelper;
    }

}
