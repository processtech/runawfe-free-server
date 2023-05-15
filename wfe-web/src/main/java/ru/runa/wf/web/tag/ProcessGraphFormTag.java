package ru.runa.wf.web.tag;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ProcessGraphImageAction;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processGraphForm")
public class ProcessGraphFormTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = -2668305021294162818L;

    private Long taskId;
    private Long childProcessId;
    private String subprocessId;

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

    @Override
    protected void fillFormData(TD td) {
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
        ProcessNodeGraphElementVisitor visitor = new ProcessNodeGraphElementVisitor(getUser(), pageContext, td, subprocessId);
        visitor.visit(elements);
        if (!visitor.getPresentationHelper().getMap().isEmpty()) {
            td.addElement(visitor.getPresentationHelper().getMap());
            img.setUseMap("#" + visitor.getPresentationHelper().getMapName());
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
