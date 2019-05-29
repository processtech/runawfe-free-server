package ru.runa.common.web.portlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PortletExceptionHandler {
    public boolean processError(Exception exception, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;
}
