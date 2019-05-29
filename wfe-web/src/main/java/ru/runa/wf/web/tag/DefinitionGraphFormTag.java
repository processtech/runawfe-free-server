package ru.runa.wf.web.tag;

import java.util.List;
import java.util.Map;

import org.apache.ecs.html.Center;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ProcessDefinitionGraphImageAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Maps;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "definitionGraphForm")
public class DefinitionGraphFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = 880745425325952663L;
    private String subprocessId;

    public String getSubprocessId() {
        return subprocessId;
    }

    @Attribute
    public void setSubprocessId(String subprocessId) {
        this.subprocessId = subprocessId;
    }

    @Override
    protected void fillFormData(final TD tdFormElement) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, getIdentifiableId());
        params.put("name", subprocessId);
        String href = Commons.getActionUrl(ProcessDefinitionGraphImageAction.ACTION_PATH, params, pageContext, PortletUrlType.Resource);
        Center center = new Center();
        IMG processGraphImage = new IMG(href);
        processGraphImage.setBorder(0);
        List<NodeGraphElement> elements = Delegates.getDefinitionService().getProcessDefinitionGraphElements(getUser(), getIdentifiableId(),
                subprocessId);
        DefinitionNodeGraphElementVisitor visitor = new DefinitionNodeGraphElementVisitor(pageContext, subprocessId);
        visitor.visit(elements);
        if (!visitor.getPresentationHelper().getMap().isEmpty()) {
            tdFormElement.addElement(visitor.getPresentationHelper().getMap());
            processGraphImage.setUseMap("#" + visitor.getPresentationHelper().getMapName());
        }
        center.addElement(processGraphImage);
        tdFormElement.addElement(center);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
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
