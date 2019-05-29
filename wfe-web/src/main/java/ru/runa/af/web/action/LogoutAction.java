package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.action.ActionBase;

/**
 * Created on 17.08.2004
 * 
 * @struts:action path="/logout" validate="false"
 * @struts.action-forward name = "success" path = "/start.do" redirect = "true"
 */
public class LogoutAction extends ActionBase {

    public static final String ACTION_NAME = "/logout";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession();
            Commons.removeUser(session);
            ProfileHttpSessionHelper.removeProfile(session);
            TabHttpSessionHelper.removeTabForwardName(session);
            session.invalidate();
        } catch (Exception e) {
            addError(request, e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
