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
package ru.runa.common.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;

/**
 * Base class for http filters.
 * 
 */
public abstract class HTTPFilterBase implements Filter {
    protected final Log log = LogFactory.getLog(getClass());

    protected abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
            ServletException;

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    protected void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        try {
            ActionMessages errors = new ActionMessages();
            ActionExceptionHelper.addException(errors, exception);
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.getRequestDispatcher("start.do").forward(request, response);
        } catch (Exception e) {
            log.error("forwarding to login page failed", e);
        }
    }
}
