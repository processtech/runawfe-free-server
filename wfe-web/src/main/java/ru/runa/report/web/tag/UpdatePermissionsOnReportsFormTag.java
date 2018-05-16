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
package ru.runa.report.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.report.web.action.UpdatePermissionsOnReportsAction;
import ru.runa.wfe.report.ReportsSecure;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updatePermissionsOnReportsForm")
public class UpdatePermissionsOnReportsFormTag extends SecuredObjectFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(getSecuredObject(), getUser(), pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected SecuredObject getSecuredObject() {
        return ReportsSecure.INSTANCE;
    }

    @Override
    protected Permission getPermission() {
        return Permission.READ;
    }

    @Override
    public String getAction() {
        return UpdatePermissionsOnReportsAction.ACTION_PATH;
    }

    @Override
    protected String getFormButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesCommon.TITLE_PERMISSION_OWNERS.message(pageContext);
    }
}
