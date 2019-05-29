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
 */
public abstract class HTTPFilterBase implements Filter {
    protected final Log log = LogFactory.getLog(getClass());

    protected abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException;

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
            ActionExceptionHelper.addException(errors, exception, request.getLocale());
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.getRequestDispatcher("start.do").forward(request, response);
        } catch (Exception e) {
            log.error("forwarding to login page failed", e);
        }
    }

    protected void forwardToPage(HttpServletRequest request, HttpServletResponse response, String page) {
        try {
            request.getRequestDispatcher(page).forward(request, response);
        } catch (Exception e) {
            log.error("forwarding to page failed", e);
        }
    }
}
