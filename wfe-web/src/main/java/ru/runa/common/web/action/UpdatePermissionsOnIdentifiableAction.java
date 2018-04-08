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
package ru.runa.common.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.UpdatePermissionsOnIdentifiableForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

abstract public class UpdatePermissionsOnIdentifiableAction extends IdentifiableAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdatePermissionsOnIdentifiableForm form = (UpdatePermissionsOnIdentifiableForm) actionForm;
        try {
            Identifiable identifiable = getIdentifiable(getLoggedUser(request), form.getId());
            List<Long> executorIds = Lists.newArrayList();
            List<Collection<Permission>> executorPermissions = Lists.newArrayList();
            SecuredObjectType type = identifiable.getSecuredObjectType();
            for (Long executorId : form.getIds()) {
                executorIds.add(executorId);
                List<Permission> permissions = Lists.newArrayList();
                for (String name : form.getPermissions(executorId).getPermissionNames()) {
                    Permission p = Permission.valueOf(name);
                    p.checkApplicable(type);
                    permissions.add(p);
                }
                executorPermissions.add(permissions);
            }
            // unset permissions
            BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            List<Executor> executors = Delegates.getAuthorizationService().getExecutorsWithPermission(getLoggedUser(request), identifiable,
                    batchPresentation, true);
            for (Executor executor : executors) {
                if (!executorIds.contains(executor.getId())) {
                    executorIds.add(executor.getId());
                    executorPermissions.add(new ArrayList<Permission>());
                }
            }
            Delegates.getAuthorizationService().setPermissions(getLoggedUser(request), executorIds, executorPermissions, identifiable);
        } catch (Exception e) {
            addError(request, e);
            return getErrorForward(getLoggedUser(request), mapping, form.getId());
        }
        return getSuccessForward(getLoggedUser(request), mapping, form.getId());
    }

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return new ArrayList<>();
    }

    public abstract ActionForward getErrorForward(ActionMapping mapping, Long identifiableId);

    public abstract ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId);

    protected ActionForward getErrorForward(User user, ActionMapping mapping, Long identifiableId) {
        return getErrorForward(mapping, identifiableId);
    }

    protected ActionForward getSuccessForward(User user, ActionMapping mapping, Long identifiableId) {
        return getSuccessForward(mapping, identifiableId);
    }
}
