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

import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.wf.web.action.UpdatePermissionsOnProcessAction;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updatePermissionsOnProcessForm")
public class UpdatePermissionsOnProcessFormTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = 8819829254395916036L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        WfProcess instance = super.getProcess();
        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(instance, getUser(), pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected Permission getPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    public String getFormButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesCommon.TITLE_PERMISSION_OWNERS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdatePermissionsOnProcessAction.ACTION_PATH;
    }
}
