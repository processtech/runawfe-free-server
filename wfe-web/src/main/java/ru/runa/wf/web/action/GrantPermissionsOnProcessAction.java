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
package ru.runa.wf.web.action;

import java.util.List;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.GrantPermisionsOnIdentifiableAction;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/grantReadPermissionOnProcess" name="idsForm"
 *                validate="true" input = "/WEB-INF/wf/manage_process.jsp"
 * @struts.action-forward name="success" path="/manage_process_permissions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_permissions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure_process_does_not_exist"
 *                        path="/manage_processes.do" redirect = "true"
 */
public class GrantPermissionsOnProcessAction extends GrantPermisionsOnIdentifiableAction {
    public static final String ACTION_PATH = "/grantReadPermissionOnProcess";

    private static final List<Permission> READ_PERMISSONS = Lists.newArrayList(Permission.READ);

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return READ_PERMISSONS;
    }

    @Override
    protected Identifiable getIdentifiable(User user, Long identifiableId) {
        return Delegates.getExecutionService().getProcess(user, identifiableId);
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, identifiableId);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, identifiableId);
    }
}
