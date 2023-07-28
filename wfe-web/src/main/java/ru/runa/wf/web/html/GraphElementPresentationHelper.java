package ru.runa.wf.web.html;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Area;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.form.TaskIdForm;
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

    private static final long DEFAULT_SELECTED_TASK_PROCESS_ID = -1;

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
     * Creates instance of helper class to create links to subprocesses in graph elements.
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
     * Creates links to subprocesses, forked in given multiple instance graph element.
     *
     * @param element
     *            Multiple instance graph element to create links.
     */
    public String createMultiSubprocessLinks(MultiSubprocessNodeGraphElement element, String action) {
        int multiLinkSize = 17;
        int maxItemsPerLine = 10;
        int additionalHeight = 0;
        int mainDivSize = multiLinkSize * element.getSubprocessIds().size();
        if (mainDivSize > maxItemsPerLine * multiLinkSize) {
            additionalHeight = (int) Math.ceil((double) mainDivSize / (maxItemsPerLine * multiLinkSize)) * multiLinkSize;
            mainDivSize = maxItemsPerLine * multiLinkSize;
        }
        int[] ltCoordinates = {
                element.getGraphConstraints()[2] - mainDivSize / 2,
                element.getGraphConstraints()[3] + multiLinkSize / 2 + additionalHeight
        };
        long selectedTaskProcessId = getSelectedTaskProcessId();
        StringBuilder builder = new StringBuilder()
                .append("<div class=\"multiInstanceContainer\" style=\"")
                .append("width: ").append(mainDivSize).append("px;")
                .append("left: ").append(ltCoordinates[0]).append("px;")
                .append("top: ").append(ltCoordinates[1]).append("px;\">");
        for (int i = 0; i < element.getSubprocessIds().size(); i++) {
            long subprocessId = element.getSubprocessIds().get(i);
            builder.append("<div class=\"multiInstanceBox\" style=\"");
            if (element.getCompletedSubprocessIds().contains(subprocessId)) {
                builder.append("background-color: ").append(DrawProperties.getHighlightColorString()).append("; ");
            }
            if (subprocessId == selectedTaskProcessId) {
                builder.append("position: relative; border-width: 2px; border-color: black;");
            }
            builder.append("width: ").append(multiLinkSize).append("px; height: ").append(multiLinkSize).append("px;\"");
            if (element.getAccessibleSubprocessIds().contains(subprocessId)) {
                builder.append(" onclick=\"window.location='").append(getSubprocessUrl(action, subprocessId)).append("';\"");
            }
            builder.append(">&nbsp;").append(i + 1).append("&nbsp;</div>");
            if ((i + 1) % maxItemsPerLine == 0) {
                builder.append("\n");
            }
        }
        return builder.append("</div>").toString();
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
     * @return {@link Area} instance with tool tip or null, if {@link Area} not created.
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

    public Area addTooltip(NodeGraphElement element, Area area, String html) {
        if (area == null) {
            area = new Area("RECT", element.getGraphConstraints());
            map.addElement(area);
        }
        area.setTitle(html);
        return area;
    }

    private long getSelectedTaskProcessId() {
        String selectedTaskProcessId = pageContext.getRequest().getParameter(TaskIdForm.SELECTED_TASK_PROCESS_ID_NAME);
        return selectedTaskProcessId != null
                ? Long.parseLong(selectedTaskProcessId)
                : DEFAULT_SELECTED_TASK_PROCESS_ID;
    }

}
