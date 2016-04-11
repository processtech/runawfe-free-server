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

import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.action.DeployProcessDefinitionAction;

/**
 * Created on 18.08.2004
 * 
 * @jsp.tag name = "deployDefinitionForm" body-content = "empty"
 */
public class DeployDefinitionFormTag extends TitledFormTag {
    private static final long serialVersionUID = -3361459425268889410L;

    protected void fillFormElement(TD tdFormElement) {
        RedeployDefinitionFormTag.fillTD(tdFormElement, getForm(), null, getUser(), pageContext);
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_DEPLOY_DEFINITION, pageContext);
    }

    public String getAction() {
        return DeployProcessDefinitionAction.ACTION_PATH;
    }

    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.DEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_DEPLOY_DEFINITION, pageContext);
    }
}
