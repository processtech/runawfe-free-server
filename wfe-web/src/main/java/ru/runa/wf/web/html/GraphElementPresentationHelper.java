package ru.runa.wf.web.html;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.view.ExclusiveGatewayGraphElement;
import ru.runa.wfe.graph.view.MultiSubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.ScriptNodeGraphElement;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.TaskNodeGraphElement;
import ru.runa.wfe.graph.view.TimerNodeGraphElement;
import ru.runa.wfe.var.VariableMapping;

/**
 * Helper class to create links to subprocesses in graph elements.
 */
public class GraphElementPresentationHelper {
    private static final String RECT = "RECT";

    private static final String TITLE = "title";

    private static final String BR_TAG = "<br>";

    private static final String NEW_LINE = "\n";

    private static final String NBSP = "&nbsp;";

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
                builder.append(NEW_LINE);
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
    public Area createSubprocessLink(SubprocessNodeGraphElement element, String action, String jsFunction, boolean showElementDefinitionDetails,
            boolean showLogs) {
        if (!element.isSubprocessAccessible()) {
            return null;
        }
        String url;
        if (element.isEmbedded()) {
            url = String.format("%s(%s, '%s', %d, %d, 'view', %b, %b)", jsFunction, element.getSubprocessId(), element.getEmbeddedSubprocessId(),
                    element.getEmbeddedSubprocessGraphWidth(), element.getEmbeddedSubprocessGraphHeight(), showElementDefinitionDetails, showLogs);
        } else {
            url = getSubprocessUrl(action, element.getSubprocessId());
        }
        Area area = new Area(RECT, element.getGraphConstraints());
        area.setHref(url);
        area.setTitle("");
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
        Area area = new Area(RECT, element.getGraphConstraints());
        area.setHref(url);
        area.setTitle(element.getName());
        map.addElement(area);
        return area;
    }

    public Table createCommonTooltip(NodeGraphElement element) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        TR trId = new TR();
        trId.addElement(((TD) new TD("ID:").setStyle("width: 50px")).setClass(Resources.CLASS_LIST_TABLE_TD));
        trId.addElement(new TD(element.getNodeId()).setClass(Resources.CLASS_LIST_TABLE_TD));
        TR trName = new TR();
        trName.addElement(new TD(MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        trName.addElement(new TD(element.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(trId);
        table.addElement(trName);
        return table;
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
        Area area = new Area(RECT, element.getGraphConstraints());
        String name = "";
        if (element.getSwimlaneName() != null) {
            name += "(" + element.getSwimlaneName() + ")" + NEW_LINE;
        }
        name += element.getName();
        area.setTitle(name);
        map.addElement(area);
        return area;
    }

    public void createTaskTooltip(TaskNodeGraphElement element, Table table) {
        if (element.isMinimized()) {
            TR tr = new TR();
            tr.addElement(((TD) new TD(MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_SWIMLANE.message(pageContext)).setStyle("width: 60px"))
                    .setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(element.getSwimlaneName()).setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(tr);
        }
        if (!Strings.isNullOrEmpty(element.getBotTaskHandlerClassName())) {
            TR trBotTaskHandler = new TR();
            trBotTaskHandler.addElement(new TD(MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_HANDLER.message(pageContext))
                    .setClass(Resources.CLASS_LIST_TABLE_TD));
            trBotTaskHandler.addElement(new TD(element.getBotTaskHandlerClassName()).setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(trBotTaskHandler);
        }
        if (!Strings.isNullOrEmpty(element.getBotTaskHandlerConfiguration())) {
            TR trBotTaskConfiguration = new TR();
            trBotTaskConfiguration.addElement(
                    new TD(MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_CONFIGURATION.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            trBotTaskConfiguration.addElement(new TD(
                    StringEscapeUtils.escapeHtml4(element.getBotTaskHandlerConfiguration()).replace(NEW_LINE, BR_TAG).replace(" ",
                            NBSP)).setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(trBotTaskConfiguration);
        }
    }

    public void createSubprocessNameTooltip(SubprocessNodeGraphElement element, Table table) {
        if (Objects.equal(element.getName(), element.getSubprocessName())) {
            return;
        }
        StrutsMessage strutsMessage = element.isEmbedded() ? MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_COMPOSITION
                : MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_SUBPROCESS;
        TR tr = new TR();
        tr.addElement(new TD(strutsMessage.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(element.getSubprocessName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(tr);
    }

    public void createVariableMappingTooltip(Table table, String title, List<VariableMapping> variableMappings, boolean withUsage) {
        if (variableMappings.isEmpty()) {
            return;
        }
        TR trHeader = new TR();
        trHeader.addElement(((TD) new TD(title).addAttribute("colspan", 2)).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(trHeader);
        for (VariableMapping mapping : variableMappings) {
            TR tr = new TR();
            tr.addElement(new TD(mapping.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(mapping.getMappedName() + (withUsage ? " " + "(" + mapping.getUsage() + ")" : ""))
                    .setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(tr);
        }
    }

    public void createTimerTooltip(TimerNodeGraphElement element, Table table) {
        addDelegationTooltip(table, element.getHandlerName(), element.getHandlerConfiguration());
    }

    public void createScriptTooltip(ScriptNodeGraphElement element, Table table) {
        addDelegationTooltip(table, element.getHandlerName(), element.getHandlerConfiguration());
    }

    public void createExclusiveGatewayTooltip(ExclusiveGatewayGraphElement element, Table table) {
        addDelegationTooltip(table, element.getHandlerName(), element.getHandlerConfiguration());
    }

    private void addDelegationTooltip(Table table, String handlerName, String handlerConfiguration) {
        if (handlerName.isEmpty()) {
            return;
        }
        TR trHandlerName = new TR();
        trHandlerName.addElement(((TD) new TD(MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_HANDLER.message(pageContext)).setStyle("width: 60px"))
                .setClass(Resources.CLASS_LIST_TABLE_TD));
        trHandlerName.addElement(new TD(handlerName).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(trHandlerName);
        TR trHandlerConfiguration = new TR();
        trHandlerConfiguration.addElement(
                new TD(MessagesProcesses.LABEL_PROCESS_GRAPH_TOOLTIP_CONFIGURATION.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        String configuration = StringEscapeUtils.escapeHtml4(handlerConfiguration).replace(NEW_LINE, BR_TAG)
                .replace("\t", NBSP + NBSP).replace(" ", NBSP);
        trHandlerConfiguration.addElement(new TD(configuration).setClass(Resources.CLASS_LIST_TABLE_TD));
        table.addElement(trHandlerConfiguration);
    }

    public Area createArea(NodeGraphElement element) {
        Area area = new Area(RECT, element.getGraphConstraints());
        area.setTitle("");
        map.addElement(area);
        return area;
    }

    public Area addTooltip(NodeGraphElement element, Area area, String html) {
        if (area == null) {
            area = new Area(RECT, element.getGraphConstraints());
            area.setTitle("");
            map.addElement(area);
        }
        area.setTitle(area.getAttribute(TITLE) + html);
        return area;
    }

    public Area createSelectElementLink(NodeGraphElement element) {
        Area area = new Area(RECT, element.getGraphConstraints());
        String url = "javascript:selectProcessNode('" + element.getNodeId() + "', true);";
        area.setHref(url);
        area.setTitle(element.getName());
        map.addElement(area);
        return area;
    }

    private long getSelectedTaskProcessId() {
        String selectedTaskProcessId = pageContext.getRequest().getParameter(TaskIdForm.SELECTED_TASK_PROCESS_ID_NAME);
        return selectedTaskProcessId != null
                ? Long.parseLong(selectedTaskProcessId)
                : DEFAULT_SELECTED_TASK_PROCESS_ID;
    }

}
