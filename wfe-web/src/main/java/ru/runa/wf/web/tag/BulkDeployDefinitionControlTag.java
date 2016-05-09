package ru.runa.wf.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.BulkDeployProcessDefinitionAction;
import ru.runa.wfe.commons.web.WebHelper;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "bulkDeployDefinitionControl")
public class BulkDeployDefinitionControlTag extends TitledFormTag {

    private static final long serialVersionUID = -3361459489654889410L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        WebHelper strutsWebHelper = new StrutsWebHelper(pageContext);
        BulkDeployDefinitionFormTag.fillTD(tdFormElement, getForm(), null, getUser(), pageContext, strutsWebHelper);
    }

    @Override
    protected String getTitle() {
        return null;
    }

    @Override
    public String getAction() {
        return BulkDeployProcessDefinitionAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.DEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected String getFormButtonName() {
        return MessagesProcesses.BUTTON_DEPLOY_DEFINITION.message(pageContext);
    }
}
