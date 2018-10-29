package ru.runa.common.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.common.web.Commons;
import ru.runa.common.web.InvalidSessionException;

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
        if (query.equals("/wfe/")) {
            try {
                Commons.getUser(request.getSession());
                forwardToPage(request, response, "manage_tasks.do");
            } catch (InvalidSessionException e) {
            }
        }
        if (query.endsWith("do") && !query.endsWith("/start.do") && !query.endsWith("login.do") && !query.endsWith("version")
                || query.endsWith("monitoring")) {
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
