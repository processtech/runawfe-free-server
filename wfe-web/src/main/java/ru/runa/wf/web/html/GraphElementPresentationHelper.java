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
package ru.runa.wf.web.html;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Area;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.view.MultiSubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.TaskNodeGraphElement;

import com.google.common.collect.Maps;

/**
 * Helper class to create links to subprocesses in graph elements.
 */
public class GraphElementPresentationHelper {
    public static final String MAP_NAME = "processMap";

    /**
     * Rendered page context.
     */
    private final PageContext pageContext;
    private final String mapName;
    /**
     * Created map of elements, represents links and tool tips areas.
     */
    private final org.apache.ecs.html.Map map = new org.apache.ecs.html.Map();

    /**
     * Creates instance of helper class to create links to subprocesses in graph
     * elements.
     *
     * @param taskId
     *            Current task identity. May be <= 0 if not applicable.
     * @param pageContext
     *            Rendered page context.
     * @param formDataTD
     *            Root form element.
     * @param map
     *            Created map of elements, represents links and tool tips areas.
     * @param linkAction
     *            Action, to be performed on subprocess link clicked.
     */
    public GraphElementPresentationHelper(PageContext pageContext, String subprocessId) {
        this.pageContext = pageContext;
        mapName = MAP_NAME + (subprocessId != null ? subprocessId : "");
        map.setName(mapName);
    }

    public String getMapName() {
        return mapName;
    }

    public org.apache.ecs.html.Map getMap() {
        return map;
    }

    /**
     * Creates links to subprocesses, forked in given multiple instance graph
     * element.
     *
     * @param element
     *            Multiple instance graph element to create links.
     */
    public String createMultiSubprocessLinks(MultiSubprocessNodeGraphElement element, String action) {
        int mlSize = 17;
        int maxItemsPerLine = 10;
        int additionalHeight = 0;
        int mainDivSize = mlSize * element.getSubprocessIds().size();
        if (mainDivSize > maxItemsPerLine * mlSize) {
            additionalHeight = (int) Math.ceil((double) mainDivSize / (maxItemsPerLine * mlSize)) * mlSize;
            mainDivSize = maxItemsPerLine * mlSize;
        }
        int[] ltCoords = new int[] { element.getGraphConstraints()[2] - mainDivSize / 2,
                element.getGraphConstraints()[3] + mlSize / 2 + additionalHeight };
        StringBuffer buf = new StringBuffer();
        buf.append("<div class=\"multiInstanceContainer\" style=\"");
        buf.append("width: ").append(mainDivSize).append("px;");
        buf.append("left: ").append(ltCoords[0]).append("px;");
        buf.append("top: ").append(ltCoords[1]).append("px;\">");
        for (int i = 0; i < element.getSubprocessIds().size(); i++) {
            Long subprocessId = element.getSubprocessIds().get(i);
            buf.append("<div class=\"multiInstanceBox\" style=\"");
            if (element.getCompletedSubprocessIds().contains(subprocessId)) {
                buf.append("background-color: ").append(DrawProperties.getHighlightColorString()).append("; ");
            }
            buf.append("width: ").append(mlSize).append("px; height: ").append(mlSize).append("px;\"");
            if (element.getAccessibleSubprocessIds().contains(subprocessId)) {
                buf.append(" onclick=\"window.location='").append(getSubprocessUrl(action, subprocessId)).append("';\"");
            }
            buf.append(">&nbsp;").append(i + 1).append("&nbsp;</div>");
            if ((i + 1) % maxItemsPerLine == 0) {
                buf.append("\n");
            }
        }
        buf.append("</div>");
        return buf.toString();
    }

    /**
     * Create link to subprocess, forked in given subprocess graph element.
     *
     * @param element
     *            Subprocess graph element to create link.
     * @return
     */
    public Area createSubprocessLink(SubprocessNodeGraphElement element, String action, String jsFunction) {
        if (!element.isSubprocessAccessible()) {
            return null;
        }
        String url;
        if (element.isEmbedded()) {
            url = jsFunction + "(" + element.getSubprocessId() + ", '" + element.getEmbeddedSubprocessId() + "', "
                    + element.getEmbeddedSubprocessGraphWidth() + ", " + element.getEmbeddedSubprocessGraphHeight() + ");";
        } else {
            url = getSubprocessUrl(action, element.getSubprocessId());
        }
        Area area = new Area("RECT", element.getGraphConstraints());
        area.setHref(url);
        area.setTitle(element.getSubprocessName());
        map.addElement(area);
        return area;
    }

    /**
     * Creates URL to subprocess with given identity.
     *
     * @param id
     *            Identity of subprocess.
     * @return URL to subprocess.
     */
    private String getSubprocessUrl(String action, Long id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, id);
        return Commons.getActionUrl(action, params, pageContext, PortletUrlType.Render);
    }

    public Area createSubprocessDefinitionLink(SubprocessNodeGraphElement element) {
        if (!element.isSubprocessAccessible()) {
            return null;
        }
        String url;
        if (element.isEmbedded()) {
            url = "javascript:showEmbeddedSubprocessDefinition(" + element.getSubprocessId() + ", '" + element.getEmbeddedSubprocessId() + "', "
                    + element.getEmbeddedSubprocessGraphWidth() + ", " + element.getEmbeddedSubprocessGraphHeight() + ");";
        } else {
            url = getSubprocessUrl(WebResources.ACTION_MAPPING_MANAGE_DEFINITION, element.getSubprocessId());
        }
        Area area = new Area("RECT", element.getGraphConstraints());
        area.setHref(url);
        area.setTitle(element.getName());
        map.addElement(area);
        return area;
    }

    /**
     * Creates tool tip for given task graph element.
     *
     * @param element
     *            Graph element, to create tool tip.
     * @return {@link Area} instance with tool tip or null, if {@link Area} not
     *         created.
     */
    public Area createTaskTooltip(TaskNodeGraphElement element) {
        if (!element.isMinimized()) {
            return null;
        }
        Area area = new Area("RECT", element.getGraphConstraints());
        String name = "";
        if (element.getSwimlaneName() != null) {
            name += "(" + element.getSwimlaneName() + ")\n";
        }
        name += element.getName();
        area.setTitle(name);
        map.addElement(area);
        return area;
    }

    public Area addTooltip(NodeGraphElement element, Area area) {
        return addTooltip(element, area, String.valueOf(element.getData()));
    }

    public Area addTooltip(NodeGraphElement element, Area area, String html) {
        if (area == null) {
            area = new Area("RECT", element.getGraphConstraints());
            map.addElement(area);
        }
        area.setTitle(html);
        return area;
    }

}
