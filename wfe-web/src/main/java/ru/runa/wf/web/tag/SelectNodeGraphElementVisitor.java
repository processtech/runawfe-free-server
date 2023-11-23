/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;
import ru.runa.wf.web.action.ShowGraphModeHelper;
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
