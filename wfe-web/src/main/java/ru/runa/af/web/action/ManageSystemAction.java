package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.Commons;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ManageSystemAction extends ForwardAction {

    private final static String TAB_FORWARD_NAME_PARAMETER_NAME = "tabForwardName";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String tabForwardName = request.getParameter(TAB_FORWARD_NAME_PARAMETER_NAME);
        if (tabForwardName != null) {
            TabHttpSessionHelper.setTabForwardName(tabForwardName, request.getSession());
        }

        User user = Commons.getUser(request.getSession());
        Permission permission = Permission.READ;
        Identifiable identifiable = ASystem.INSTANCE;
        boolean isAllowed = Delegates.getAuthorizationService().isAllowed(user, permission, identifiable);
        if (!isAllowed) {
            throw new AuthorizationException(user + " does not have " + permission + " to " + identifiable);
        }

        return super.execute(mapping, form, request, response);
    }

}
