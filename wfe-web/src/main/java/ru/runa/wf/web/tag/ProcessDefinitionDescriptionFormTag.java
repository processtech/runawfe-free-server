package ru.runa.wf.web.tag;

import org.apache.ecs.html.IFrame;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.ProcessDefinitionDescriptionAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "processDefinitionDescriptionForm")
public class ProcessDefinitionDescriptionFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(final TD tdFormElement) {
        Long id = ((WfDefinition) getSecuredObject()).getId();
        String url = Commons.getActionUrl(ProcessDefinitionDescriptionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, id, pageContext,
                PortletUrlType.Action);
        tdFormElement.addElement(new IFrame().setSrc(url).setWidth("100%"));
    }

    @Override
    protected boolean isVisible() {
        DefinitionService definitionService = Delegates.getDefinitionService();
        return definitionService.getProcessDefinitionFile(getUser(), getIdentifiableId(), ProcessDefinitionDescriptionAction.DESCRIPTION_FILE_NAME) != null;
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
        return Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.DESCRIPTION, pageContext);
    }

}
