package ru.runa.wf.web.tag;

import org.apache.ecs.html.TD;

import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.action.BulkDeployProcessDefinitionAction;
import ru.runa.wfe.commons.web.WebHelper;

/**
 * Created 26.05.2014
 * 
 * @jsp.tag name = "bulkDeployDefinitionControl" body-content = "empty"
 */
public class BulkDeployDefinitionControlTag extends TitledFormTag {
	
	private static final long serialVersionUID = -3361459489654889410L;

    protected void fillFormElement(TD tdFormElement) {
    	WebHelper strutsWebHelper = new StrutsWebHelper(pageContext);
    	BulkDeployDefinitionFormTag.fillTD(tdFormElement, getForm(), null, getUser(), pageContext, strutsWebHelper);
    }

    protected String getTitle() {
        return null;
    }

    public String getAction() {
        return BulkDeployProcessDefinitionAction.ACTION_PATH;
    }

    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.DEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_DEPLOY_DEFINITION, pageContext);
    }
}
