package ru.runa.af.web.tag;

import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.DeployBotAction;
import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployBot")
public class DeployBotTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    private Long botStationId;

    @Attribute(required = false, rtexprvalue = true)
    public void setBotStationId(Long botStationId) {
        this.botStationId = botStationId;
    }

    public Long getBotStationId() {
        return botStationId;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.BOTSTATIONS);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesBot.BUTTON_DEPLOY_BOT.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.BUTTON_DEPLOY_BOT.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeployBotAction.ACTION_PATH;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setEncType(Form.ENC_UPLOAD);
        tdFormElement.addElement(new Input(Input.hidden, DeployBotForm.BOT_STATION_ID, Long.toString(botStationId)));
        Input boolInput = new Input(Input.CHECKBOX, DeployBotForm.REPLACE_OPTION_NAME);
        tdFormElement.addElement(boolInput);
        tdFormElement.addElement(MessagesBot.LABEL_REPLACE_BOT_TASKS.message(pageContext) + "<br>");

        if (WebResources.isBulkDeploymentElements()) {
            String fileUploadInput = ViewUtil.getFileInput(new StrutsWebHelper(pageContext), FileForm.FILE_INPUT_NAME, true);
            tdFormElement.addElement(fileUploadInput);
        } else {
            Input fileUploadInput = new Input(Input.FILE, FileForm.FILE_INPUT_NAME);
            fileUploadInput.setClass(Resources.CLASS_REQUIRED);
            tdFormElement.addElement(fileUploadInput);
        }
    }
}
