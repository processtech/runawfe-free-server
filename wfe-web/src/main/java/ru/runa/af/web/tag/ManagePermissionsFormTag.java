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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.UpdatePermissionsAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag2;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.SystemExecutors;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "managePermissionsForm")
public class ManagePermissionsFormTag extends SecuredObjectFormTag2 {
    private static final long serialVersionUID = 1L;

    @Override
    public String getAction() {
        return UpdatePermissionsAction.ACTION_PATH;
    }

    @Override
    public final void fillFormElement(TD tdFormElement) {
        SecuredObject o = getSecuredObject();
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.READ_PERMISSIONS, o);
        super.fillFormElement(tdFormElement);

        // This is for UpdatePermissionsAction to return back to current page:
        tdFormElement.addElement(new Input(Input.HIDDEN, "returnAction", Commons.getSelfActionWithQueryString(pageContext)));

        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(o, getUser(), pageContext);
        if (o.getSecuredObjectType() == SecuredObjectType.DEFINITION) {
            tableBuilder.addAdditionalExecutor(
                    Delegates.getExecutorService().getExecutorByName(getUser(), SystemExecutors.PROCESS_STARTER_NAME),
                    Lists.newArrayList(Iterables.filter(ApplicablePermissions.listVisible(o), new Predicate<Permission>() {
                        @Override
                        public boolean apply(Permission p) {
                            return p != Permission.READ_PROCESS && p != Permission.CANCEL_PROCESS;
                        }
                    }))
            );
        }
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesCommon.TITLE_PERMISSION_OWNERS.message(pageContext);
    }
}
