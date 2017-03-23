package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.wfe.user.User;

public abstract class ActionBase extends Action {
    protected final Log log = LogFactory.getLog(getClass());

    protected User getLoggedUser(HttpServletRequest request) {
        return Commons.getUser(request.getSession());
    }

    protected void addError(HttpServletRequest request, Exception e) {
        ActionMessages errors = getActionMessages(request, Globals.ERROR_KEY);
        ActionExceptionHelper.addException(errors, e);
        saveErrors(request.getSession(), errors);
    }

    protected void addError(HttpServletRequest request, ActionMessage message) {
        ActionMessages errors = getActionMessages(request, Globals.ERROR_KEY);
        errors.add(ActionMessages.GLOBAL_MESSAGE, message);
        saveErrors(request.getSession(), errors);
    }

    protected void addMessage(HttpServletRequest request, ActionMessage message) {
        ActionMessages messages = getActionMessages(request, Globals.MESSAGE_KEY);
        messages.add(ActionMessages.GLOBAL_MESSAGE, message);
        saveMessages(request.getSession(), messages);
    }

    private ActionMessages getActionMessages(HttpServletRequest request, String key) {
        ActionMessages errors = (ActionMessages) request.getAttribute(key);
        if (errors == null) {
            errors = (ActionMessages) request.getSession().getAttribute(key);
        }
        if (errors == null) {
            errors = new ActionMessages();
        }
        return errors;
    }

}
