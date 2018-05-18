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
package ru.runa.af.web.action;

import com.google.common.collect.Lists;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.GrantPermissionsForm;
import ru.runa.common.web.PermissionWebUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.security.SecuredObjectFactory;

/**
 * @struts:action path="/grantPermissions" name="grantPermissionsForm" validate="false"
 */
public class GrantPermissionsAction extends ActionBase {

    public static final String ACTION_PATH = "/grantPermissions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        GrantPermissionsForm form = (GrantPermissionsForm) actionForm;
        List<Long> ids = Lists.newArrayList(form.getIds());
        try {
            SecuredObject obj = SecuredObjectFactory.getInstance().findById(SecuredObjectType.valueOf(form.getSecuredObjectType()), form.getId());
            Delegates.getAuthorizationService().setPermissions(getLoggedUser(request), ids, ApplicablePermissions.getDefaults(obj), obj);
        } catch (Exception e) {
            addError(request, e);
        }

        return PermissionWebUtils.getReturnActionForward(form);
    }
}
