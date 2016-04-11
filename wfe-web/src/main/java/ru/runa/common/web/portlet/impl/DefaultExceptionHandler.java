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
package ru.runa.common.web.portlet.impl;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.InvalidSessionException;
import ru.runa.common.web.portlet.PortletExceptionHandler;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

public class DefaultExceptionHandler implements PortletExceptionHandler {
    @Override
    public boolean processError(Exception exception, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        if (exception instanceof AuthenticationException || exception instanceof AuthenticationExpiredException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/start.do").forward(request, response);
            return true;
        }

        if (exception instanceof InvalidSessionException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/start.do").forward(request, response);
            return true;
        }

        if (exception instanceof ExecutorDoesNotExistException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/manage_executors.do").forward(request, response);
            return true;
        }

        if (exception instanceof DefinitionDoesNotExistException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/manage_process_definitions.do").forward(request, response);
            return true;
        }

        if (exception instanceof ProcessDoesNotExistException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/manage_processes.do").forward(request, response);
            return true;
        }

        if (exception instanceof TaskDoesNotExistException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/manage_tasks.do").forward(request, response);
            return true;
        }

        if (exception instanceof java.lang.RuntimeException) {
            ActionExceptionHelper.addException(getActionErrors(request), exception);
            servletContext.getRequestDispatcher("/error.do").forward(request, response);
            return true;
        }

        return false;
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
