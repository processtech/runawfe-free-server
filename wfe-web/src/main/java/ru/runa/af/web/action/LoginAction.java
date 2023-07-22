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

import ru.runa.af.web.form.LoginForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import java.util.Enumeration;
import java.util.Map;

/**
 * Created on 09.08.2004
 *
 * @struts:action path="/login" name="loginForm" validate="true" input =
 *                "/start.do"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/start.do" redirect = "true"
 */
public class LoginAction extends ActionBase {

    private final static String DEFAULT_TAB_FORWARD_NAME = "manage_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginForm form = (LoginForm) actionForm;
            User user = Delegates.getAuthenticationService().authenticateByLoginPassword(form.getLogin(), form.getPassword());
            Delegates.getSystemService().login(user);
            Commons.setUser(user, request.getSession());
            ProfileHttpSessionHelper.reloadProfile(request.getSession());
            TabHttpSessionHelper.setTabForwardName(DEFAULT_TAB_FORWARD_NAME, request.getSession());
            saveToken(request);
            if (request.getParameter("forwardUrl") != null && request.getParameter("forwardUrl").isEmpty() != true) {
                return new ActionForward(request.getParameter("forwardUrl"), true);
            } else {
                return mapping.findForward(Resources.FORWARD_SUCCESS);
            }
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }

}
