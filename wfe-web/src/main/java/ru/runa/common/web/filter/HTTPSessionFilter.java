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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.common.web.Commons;
import ru.runa.common.web.InvalidSessionException;
import ru.runa.wfe.user.User;

/**
 * This filter checks that the user session is active.
 * 
 * @web.filter name="session"
 * @web.filter-mapping url-pattern = "/*"
 */
public class HTTPSessionFilter extends HTTPFilterBase {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String query = request.getRequestURI();
        User user = (User) Commons.getSessionAttribute(request.getSession(), User.class.getName());
        if (user != null && query.equals("/wfe/")){
            forwardToPage(request, response, "manage_tasks.do");
        }
        if (query.endsWith("do") && !query.endsWith("/start.do") && !query.endsWith("login.do") && !query.endsWith("version")) {
            try {
                Commons.getUser(request.getSession());
            } catch (InvalidSessionException e) {
                request.setAttribute("forwardUrl", request.getRequestURI());
                forwardToLoginPage(request, response, e);
                return;
            }
        }
        chain.doFilter(request, response);
    }

}
