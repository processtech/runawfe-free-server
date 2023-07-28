package ru.runa.wf.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.DeployProcessDefinitionAction;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployDefinitionForm")
public class DeployDefinitionFormTag extends TitledFormTag {
    private static final long serialVersionUID = -3361459425268889410L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        RedeployDefinitionFormTag.getInstance().fillTD(tdFormElement, getForm(), null, getUser(), pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_DEPLOY_DEFINITION.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeployProcessDefinitionAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.DEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_DEPLOY_DEFINITION.message(pageContext);
    }
}
