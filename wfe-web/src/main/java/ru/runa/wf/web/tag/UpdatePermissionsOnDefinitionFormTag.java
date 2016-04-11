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

import java.util.List;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Messages;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.wf.web.action.UpdatePermissionsOnProcessDefinitionAction;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.SystemExecutors;

/**
 * Created on 24.08.2004
 * 
 * @jsp.tag name = "updatePermissionsOnDefinitionForm" body-content = "JSP"
 */
public class UpdatePermissionsOnDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = -3924515617058954059L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        WfDefinition definition = getDefinition();
        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(definition, getUser(), pageContext);
        Actor starter = Delegates.getExecutorService().getExecutorByName(getUser(), SystemExecutors.PROCESS_STARTER_NAME);
        tableBuilder.addAdditionalExecutor(starter, getUnmodifiablePermissions());
        Table table = tableBuilder.buildTable();
        tdFormElement.addElement(table);
    }

    private List<Permission> getUnmodifiablePermissions() {
        List<Permission> result = new DefinitionPermission().getAllPermissions();
        result.remove(DefinitionPermission.CANCEL_STARTED_PROCESS);
        result.remove(DefinitionPermission.READ_STARTED_PROCESS);
        return result;
    }

    @Override
    protected Permission getPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PERMISSION_OWNERS, pageContext);
    }

    @Override
    public String getAction() {
        return UpdatePermissionsOnProcessDefinitionAction.ACTION_PATH;
    }

}
