package ru.runa.wfe.rest.auth;

import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.runa.wfe.auth.JwtUser;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.rest.config.SpringSecurityConfig;
import ru.runa.wfe.security.SecuredObjectUtil;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

import static java.util.Collections.singletonList;

@CommonsLog
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private TransactionalExecutor transactionalExecutor;
    @Transactional
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(SecuredObjectUtil.BEARER_PREFIX)) {
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
            return;
        }
        header = header.replace(SecuredObjectUtil.BEARER_PREFIX, "");

        try {
            final User dirtyUser = new JwtUser().with(header);
            final Actor actor = getActor(request, dirtyUser.getActor().getId());
            final User user = new User(actor, dirtyUser.getSecuredKey());

            final List<GrantedAuthority> authorities = singletonList(new SimpleGrantedAuthority("ROLE_" + SpringSecurityConfig.ROLE));
            final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    new AuthUser(user),
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("", e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    // TODO newweb костыль на скорую руку
    // do not load from db for performance (if possible; or make code better here)
    // SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    // https://stackoverflow.com/questions/48107157/autowired-is-null-in-usernamepasswordauthenticationfilter-spring-boot
    private ExecutorDao executorDao;

    private Actor getActor(HttpServletRequest request, Long actorId) {
        if (executorDao == null) {
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            executorDao = webApplicationContext.getBean(ExecutorDao.class);
        }
        return (Actor) transactionalExecutor.executeWithResult(() -> {
            return executorDao.getActor(actorId);
        });
        // Actor actor = new Actor(claims.getSubject(), null);
        // Long actorId = ((Number) claims.get(USER_ACTOR_ID_ATTRIBUTE_NAME)).longValue();
        // actor.setId(actorId);
        // org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing:
        // ru.runa.wfe.user.Actor
        // at org.hibernate.engine.internal.ForeignKeys.getEntityIdentifierIfNotUnsaved(ForeignKeys.java:279)
    }
}