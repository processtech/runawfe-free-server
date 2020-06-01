package ru.runa.af.web.action;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.BaseAction;
import ru.runa.common.web.Commons;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
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
        // TODO Checking READ permission for CREATE operation?! See #1586-5, #1586-6.
        //      BTW, UpdateSubstitutionCriteriaAction and DeleteSubstitutionCriteriasAction don't check any permissions at all.
        Permission permission = Permission.READ;
        SecuredObject object = SecuredSingleton.SYSTEM;
        boolean isAllowed = Delegates.getAuthorizationService().isAllowed(user, permission, object);
        if (!isAllowed) {
            throw new AuthorizationException(user + " does not have " + permission + " to " + object);
        }

        return new ActionForward(path);
    }
}
