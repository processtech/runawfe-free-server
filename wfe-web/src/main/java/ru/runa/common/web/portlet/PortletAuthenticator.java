package ru.runa.common.web.portlet;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PortletAuthenticator {
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response, PortletSession session);
}
