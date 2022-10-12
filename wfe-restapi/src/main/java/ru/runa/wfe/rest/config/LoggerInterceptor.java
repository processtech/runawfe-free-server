package ru.runa.wfe.rest.config;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CommonsLog
public class LoggerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("Request received for " + request.getMethod() + " " + getRequestPath(request));
        return super.preHandle(request, response, handler);
    }

    private String getRequestPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        String queryString = request.getQueryString();
        String secureQueryString = queryString == null ? "" : "?" + queryString.replaceAll("password=[^&]*", "password=****");
        return "'" + (pathInfo != null ? pathInfo : "") + secureQueryString + "'";
    }
}
