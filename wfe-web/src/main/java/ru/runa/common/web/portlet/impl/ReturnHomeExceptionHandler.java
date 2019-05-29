package ru.runa.common.web.portlet.impl;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.portlet.PortletExceptionHandler;

public class ReturnHomeExceptionHandler implements PortletExceptionHandler {

    @Override
    public boolean processError(Exception exception, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        ActionExceptionHelper.addException(getActionErrors(request), exception, request.getLocale());
        String thinInterfacePage = (String) request.getAttribute("runawfe.thin.interface.page");
        if (thinInterfacePage == null) {
            thinInterfacePage = "/start.do";
        }
        servletContext.getRequestDispatcher(thinInterfacePage).forward(request, response);
        return true;
    }

    private static ActionMessages getActionErrors(HttpServletRequest request) {
        ActionMessages messages = (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
        if (messages == null) {
            messages = new ActionMessages();
            request.setAttribute(Globals.ERROR_KEY, messages);
        }
        return messages;
    }
}
