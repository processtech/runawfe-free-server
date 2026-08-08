package ru.runa.common.web.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.runa.common.web.Commons;
import ru.runa.common.web.InvalidSessionException;

/**
 * Фильтр аутентификации для всех /api/* эндпоинтов.
 * по аналогии с HTTPSessionFilter
 */
public class APIAuthenticationFilter extends HTTPFilterBase {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Пропускаем пути, не требующие аутентификации
        if (uri.endsWith("/login.do") || uri.endsWith("/start.do") || uri.endsWith("/version")) {
            chain.doFilter(request, response);
            return;
        }

        // Проверяем сессию для ВСЕХ остальных запросов (включая /api/*)
        try {
            Commons.getUser(request.getSession()); // Бросит исключение при недействительной сессии
            chain.doFilter(request, response);
        } catch (InvalidSessionException e) {
            // Для API возвращаем 401, для веб-интерфейса — редирект на логин
            if (uri.startsWith(contextPath + "/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            } else {
                request.setAttribute("forwardUrl", uri);
                forwardToLoginPage(request, response, e);
            }
        }
    }
}