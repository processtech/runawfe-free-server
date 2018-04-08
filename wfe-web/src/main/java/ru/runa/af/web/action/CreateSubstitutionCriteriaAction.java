package ru.runa.af.web.action;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.BaseAction;

import ru.runa.common.web.Commons;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class CreateSubstitutionCriteriaAction extends BaseAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = mapping.getParameter();

        if (path == null) {
            throw new ServletException(messages.getMessage("forward.path"));
        }

        User user = Commons.getUser(request.getSession());
        Permission permission = Permission.READ;
        Identifiable identifiable = ASystem.INSTANCE;
        boolean isAllowed = Delegates.getAuthorizationService().isAllowed(user, permission, identifiable);
        if (!isAllowed) {
            throw new AuthorizationException(user + " does not have " + permission + " to " + identifiable);
        }

        return new ActionForward(path);
    }
}
