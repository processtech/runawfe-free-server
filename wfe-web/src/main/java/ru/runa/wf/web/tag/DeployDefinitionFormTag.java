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
    protected String getFormButtonName() {
        return MessagesProcesses.BUTTON_DEPLOY_DEFINITION.message(pageContext);
    }
}
