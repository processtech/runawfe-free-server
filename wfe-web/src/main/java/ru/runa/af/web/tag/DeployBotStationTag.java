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
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.DeployBotStationAction;
import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployBotStation")
public class DeployBotStationTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.BOTSTATIONS);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesBot.BUTTON_DEPLOY_BOT_STATION.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.BUTTON_DEPLOY_BOT_STATION.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeployBotStationAction.ACTION_PATH;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setEncType(Form.ENC_UPLOAD);
        Input boolInput = new Input(Input.CHECKBOX, DeployBotForm.REPLACE_OPTION_NAME);
        tdFormElement.addElement(boolInput);
        tdFormElement.addElement(MessagesBot.LABEL_REPLACE_BOT_TASKS.message(pageContext) + "<br>");
        Input fileUploadInput = new Input(Input.FILE, FileForm.FILE_INPUT_NAME);
        fileUploadInput.setClass(Resources.CLASS_REQUIRED);
        tdFormElement.addElement(fileUploadInput);
    }
}
