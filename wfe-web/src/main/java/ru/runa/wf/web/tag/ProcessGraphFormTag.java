package ru.runa.wf.web.tag;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ProcessGraphImageAction;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processGraphForm")
public class ProcessGraphFormTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = 1L;
    private static final String CHECKED_FIELD = "CHECKED_FIELD";
    private static final String ACTION_PATH = "/manage_process";
    private static final String SHOW_ELEMENT_DEFINITION_DETAILS = "showElementDefinitionDetails";
    private static final String SHOW_LOGS = "showLogs";

    private Long taskId;
    private Long childProcessId;
    private String subprocessId;
    private String graphMode;

    public ProcessGraphFormTag() {
        this.id = "processGraph";
    }

    public Long getTaskId() {
        return taskId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getChildProcessId() {
        return childProcessId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setChildProcessId(Long childProcessId) {
        this.childProcessId = childProcessId;
    }

    public String getSubprocessId() {
        return subprocessId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setSubprocessId(String subprocessId) {
        this.subprocessId = subprocessId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setGraphMode(String graphMode) {
        this.graphMode = graphMode;
    }

    @Override
    protected void fillFormData(TD td) {
        getForm().setID("processGraphForm");
        boolean showElementDefinitionDetails = Optional.ofNullable(pageContext.getRequest().getParameter(SHOW_ELEMENT_DEFINITION_DETAILS))
                .map(Boolean::parseBoolean).orElse(false);
        boolean showLogs = Optional.ofNullable(pageContext.getRequest().getParameter(SHOW_LOGS))
                .map(Boolean::parseBoolean).orElse(true);

        if (Strings.isNullOrEmpty(subprocessId)) {
            Table table = new Table();
            TD checkboxTd = new TD();

            Input showElementDefinitionDetailsCheckbox = new Input(Input.CHECKBOX, SHOW_ELEMENT_DEFINITION_DETAILS)
                    .setChecked(showElementDefinitionDetails);
            String showElementDefinitionDetailsActionUrl = Commons.getActionUrl(ACTION_PATH,
                    ImmutableMap.of("id", getProcess().getId(), SHOW_LOGS, showLogs, SHOW_ELEMENT_DEFINITION_DETAILS, CHECKED_FIELD), pageContext,
                    PortletUrlType.Action);
            showElementDefinitionDetailsCheckbox
                    .setOnChange("updateGraphView('" + showElementDefinitionDetailsActionUrl + "#processGraph', this.checked)");
            checkboxTd.addElement(showElementDefinitionDetailsCheckbox);
            checkboxTd.addElement(MessagesProcesses.LABEL_PROCESS_GRAPH_SHOW_ELEMENT_DETAILS.message(pageContext));

            if (DrawProperties.isLogsInGraphEnabled()) {
                Input showLogsCheckbox = new Input(Input.CHECKBOX, SHOW_LOGS).setChecked(showLogs);
                showLogsCheckbox.setStyle("margin-left: 30px");
                String showLogsActionUrl = Commons.getActionUrl(ACTION_PATH, ImmutableMap.of("id", getProcess().getId(),
                        SHOW_ELEMENT_DEFINITION_DETAILS, showElementDefinitionDetails, SHOW_LOGS, CHECKED_FIELD), pageContext, PortletUrlType.Action);
                showLogsCheckbox.setOnChange("updateGraphView('" + showLogsActionUrl + "#processGraph', this.checked)");
                checkboxTd.addElement(showLogsCheckbox);
                checkboxTd.addElement(MessagesProcesses.LABEL_PROCESS_GRAPH_SHOW_LOGS.message(pageContext));
            }

            table.addElement(new TR(checkboxTd));
            td.addElement(table);
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, getProcess().getId());
        params.put("childProcessId", childProcessId);
        params.put("name", subprocessId);
        params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
        String href = Commons.getActionUrl(ProcessGraphImageAction.ACTION_PATH, params, pageContext, PortletUrlType.Resource);
        IMG img = new IMG();
        img.setID("graph");
        img.setSrc(href);
        img.setBorder(0);
        List<NodeGraphElement> elements = Delegates.getExecutionService().getProcessDiagramElements(getUser(), getIdentifiableId(), subprocessId);
        GraphElementPresentationHelper helper;
        if ("Select".equals(graphMode)) {
            SelectNodeGraphElementVisitor visitor = new SelectNodeGraphElementVisitor(pageContext, td, subprocessId);
            visitor.visit(elements);
            helper = visitor.getPresentationHelper();
        } else {
            ProcessNodeGraphElementVisitor visitor = new ProcessNodeGraphElementVisitor(getUser(), pageContext, td, subprocessId,
                    showElementDefinitionDetails, showLogs);
            visitor.visit(elements);
            helper = visitor.getPresentationHelper();
        }
        if (!helper.getMap().isEmpty()) {
            td.addElement(helper.getMap());
            img.setUseMap("#" + helper.getMapName());
        }
        td.addElement(img);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.READ;
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        if (subprocessId != null) {
            return null;
        }
        return MessagesProcesses.TITLE_PROCESS_GRAPH.message(pageContext);
    }
}
