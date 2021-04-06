package ru.runa.common.web.portlet.impl;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticateIfPossible extends AuthenticateMandatory {

    public boolean authenticate(HttpServletRequest request, HttpServletResponse response, PortletSession session) {
        silent = true;
        super.authenticate(request, response, session);
        // Return true in all cases. If we can't auth - we must continue request processing
        return true;
    }
}
