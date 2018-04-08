/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Permission;
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
    protected boolean isFormButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
    }

    @Override
    protected String getFormButtonName() {
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
