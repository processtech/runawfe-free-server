/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ftl.AjaxFormComponent;

import com.google.common.base.Objects;

public class AjaxFormComponentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AjaxFormComponentServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                if (list == null || list.size() < 1) {
                    throw new NullPointerException("No components found in session by " + sessionKey);
                }
                AjaxFormComponent formComponent = null;
                if (qualifier != null) {
                    for (AjaxFormComponent component : list) {
                        if (Objects.equal(component.getQualifier(), qualifier)) {
                            formComponent = component;
                            break;
                        }
                    }
                    if (formComponent == null) {
                        throw new NullPointerException("No component found by qualifier '" + qualifier + "', components in session: " + list);
                    }
                } else {
                    formComponent = list.get(0);
                }
                formComponent.processAjaxRequest(request, response);
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
