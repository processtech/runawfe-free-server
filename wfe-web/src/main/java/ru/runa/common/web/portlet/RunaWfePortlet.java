package ru.runa.common.web.portlet;

import java.io.IOException;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.portals.bridges.struts.StrutsPortlet;
import org.apache.portals.bridges.struts.StrutsPortletErrorContext;
import ru.runa.common.web.portlet.impl.AuthenticateIfPossible;
import ru.runa.common.web.portlet.impl.DefaultExceptionHandler;
import ru.runa.wfe.commons.ClassLoaderUtil;

@Deprecated
public class RunaWfePortlet extends StrutsPortlet {

    HttpServletRequest portletRequest;
    HttpServletResponse portletResponse;
    PortletAuthenticator authenticator;
    PortletExceptionHandler exceptionHandler;
    boolean thinInterface = false;
    String thinInterfacePage;

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        String authenticatorClassName = getInitParameter("runawfe.authenticator");
        String errorHandlerClassName = getInitParameter("runawfe.exceptionHandler");
        String thin = getInitParameter("runawfe.thinInterface");
        thinInterfacePage = getInitParameter("ViewPage");
        if (authenticatorClassName != null) {
            authenticator = ClassLoaderUtil.instantiate(authenticatorClassName);
        }
        if (authenticator == null) {
            authenticator = new AuthenticateIfPossible();
        }
        if (errorHandlerClassName != null) {
            exceptionHandler = ClassLoaderUtil.instantiate(errorHandlerClassName);
        }
        if (exceptionHandler == null) {
            exceptionHandler = new DefaultExceptionHandler();
        }
        if (thin != null && thin.equalsIgnoreCase("true")) {
            thinInterface = true;
        }
    }

    @Override
    protected void processRequest(PortletRequest request, PortletResponse response, String defaultPage, String requestType) throws PortletException,
            IOException {
        portletRequest = getServletContextProvider().getHttpServletRequest(this, request);
        portletResponse = getServletContextProvider().getHttpServletResponse(this, response);
        if (thinInterface) {
            portletRequest.setAttribute("runawfe.thin.interface", "true");
        }
        portletRequest.setAttribute("runawfe.thin.interface.page", thinInterfacePage);
        if (!authenticator.authenticate(portletRequest, portletResponse, request.getPortletSession())) {
            return;
        }

        super.processRequest(request, response, defaultPage, requestType);
        return;
    }

    @Override
    protected void renderError(HttpServletResponse response, StrutsPortletErrorContext errorContext) throws IOException {
        try {
            Exception exception = errorContext.getError();
            while (exception != null
                    && !exceptionHandler.processError(exception, getServletContextProvider().getServletContext(this), portletRequest, response)) {
                exception = (Exception) exception.getCause();
            }
        } catch (ServletException e) {
            super.renderError(response, errorContext);
        }
    }
}
