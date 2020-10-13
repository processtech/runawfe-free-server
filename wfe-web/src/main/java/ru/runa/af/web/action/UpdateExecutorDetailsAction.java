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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/**
 * Created on 19.08.2004
 * 
 * @struts:action path="/updateExecutorDetails" name="updateExecutorDetailsForm" validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure_executor_does_not_exist" path="/manage_executors.do" redirect = "true"
 */
public class UpdateExecutorDetailsAction extends ActionBase {

    public static final String ACTION_PATH = "/updateExecutorDetails";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdateExecutorDetailsForm form = (UpdateExecutorDetailsForm) actionForm;
        try {
            ExecutorService executorService = Delegates.getExecutorService();
            Executor executor = executorService.getExecutor(getLoggedUser(request), form.getId());
            executor.setDescription(form.getDescription());
            executor.setFullName(form.getFullName());
            executor.setName(form.getNewName());
            if (executor instanceof Actor) {
                Actor actor = (Actor) executor;
                actor.setCode(form.getCode());
                actor.setPhone(form.getPhone());
                actor.setEmail(form.getEmail());
                actor.setTitle(form.getTitle());
                actor.setDepartment(form.getDepartment());
            } else {
                Group group = (Group) executor;
                group.setLdapGroupName(form.getEmail());
            }
            executorService.update(getLoggedUser(request), executor);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }

}
