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
    protected Permission getPermission() {
        return Permission.READ;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(DefinitionClassPresentation.DESCRIPTION, pageContext);
    }

}
