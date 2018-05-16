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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.GrantPermisionsOnSecuredObjectAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class GrantPermissionsOnRelationAction extends GrantPermisionsOnSecuredObjectAction {
    public static final String ACTION_PATH = "/grantPermissionsOnRelation";

    private static final List<Permission> PERMISSIONS = Lists.newArrayList(Permission.READ);

    @Override
    protected List<Permission> getSecuredObjectPermissions() {
        return PERMISSIONS;
    }

    @Override
    protected SecuredObject getSecuredObject(User user, Long identifiableId) {
        return Delegates.getRelationService().getRelation(user, identifiableId);
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        Map<String, Object> params = new HashMap<>();
        params.put(IdForm.ID_INPUT_NAME, identifiableId);
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        Map<String, Object> params = new HashMap<>();
        params.put(IdForm.ID_INPUT_NAME, identifiableId);
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }

}
