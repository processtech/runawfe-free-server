package ru.runa.common.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XSSFilter extends HTTPFilterBase {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String query = request.getRequestURI();
        if (!query.contains("update_tasks_handler_conf") && !query.contains("admin_scripts")) {
            request = new XSSRequestWrapper(request);
        }
        chain.doFilter(request, response);
    }

}
