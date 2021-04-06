package ru.runa.wf.web.servlet;

import com.google.common.base.Objects;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ftl.AjaxFormComponent;

@CommonsLog
public class AjaxFormComponentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            log.debug("Got ajax request: " + request.getQueryString());
            long startTime = System.currentTimeMillis();
            try {
                String componentName = request.getParameter("component");
                String sessionKey = AjaxFormComponent.COMPONENT_SESSION_PREFIX + componentName;
                String qualifier = request.getParameter("qualifier");
                List<AjaxFormComponent> list = (List<AjaxFormComponent>) request.getSession().getAttribute(sessionKey);
                if (list == null || list.isEmpty()) {
                    throw new NullPointerException("No components found in session by " + sessionKey);
                }
                AjaxFormComponent component = null;
                for (AjaxFormComponent testComponent : list) {
                    if (Objects.equal(testComponent.getVariableNameForSubmissionProcessing(), qualifier)) {
                        component = testComponent;
                        break;
                    }
                }
                if (component == null) {
                    throw new NullPointerException("No component found by qualifier '" + qualifier + "', components found: " + list.size());
                }
                component.processAjaxRequest(request, response);
            } catch (Exception e) {
                log.error("", e);
                throw new ServletException(e);
            }
            long endTime = System.currentTimeMillis();
            log.debug("Request processed for (ms): " + (endTime - startTime));
        } catch (Exception e) {
            log.error("ajax", e);
            throw new ServletException(e);
        }
    }
}
