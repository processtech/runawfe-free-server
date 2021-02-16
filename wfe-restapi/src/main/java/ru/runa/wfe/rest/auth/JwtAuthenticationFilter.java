package ru.runa.wfe.rest.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.rest.config.SpringSecurityConfig;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final Key JWT_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    public static final String USER_ACTOR_ID_ATTRIBUTE_NAME = "uid";
    public static final String USER_SECURED_KEY_ATTRIBUTE_NAME = "usk";

    @Transactional
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            Claims claims = getTokenClaims(request);
            if (claims != null) {
                setUpSpringAuthentication(request, claims);
            } else {
                SecurityContextHolder.clearContext();
            }
            chain.doFilter(request, response);
        } catch (JwtException e) {
            e.printStackTrace(); // TODO newweb temporary, use logging
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }
    }

    private void setUpSpringAuthentication(HttpServletRequest request, Claims claims) {
        Long actorId = ((Number) claims.get(USER_ACTOR_ID_ATTRIBUTE_NAME)).longValue();
        Actor actor = getActor(request, actorId);
        byte[] securedKey = Base64.getDecoder().decode((String) claims.get(USER_SECURED_KEY_ATTRIBUTE_NAME));
        User user = new User(actor, securedKey);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + SpringSecurityConfig.ROLE));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(new AuthUser(user), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

    }

    private Claims getTokenClaims(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY).build().parseClaimsJws(header.replace(BEARER_PREFIX, "")).getBody();
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
        ActorLoader actorLoader = new ActorLoader(actorId);
        actorLoader.executeInTransaction(true);
        return actorLoader.actor;
        // Actor actor = new Actor(claims.getSubject(), null);
        // Long actorId = ((Number) claims.get(USER_ACTOR_ID_ATTRIBUTE_NAME)).longValue();
        // actor.setId(actorId);
        // org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing:
        // ru.runa.wfe.user.Actor
        // at org.hibernate.engine.internal.ForeignKeys.getEntityIdentifierIfNotUnsaved(ForeignKeys.java:279)
    }

    private class ActorLoader extends TransactionalExecutor {
        final Long actorId;
        Actor actor;

        public ActorLoader(Long actorId) {
            this.actorId = actorId;
        }

        @Override
        protected void doExecuteInTransaction() throws Exception {
            actor = executorDao.getActor(actorId);
        }

    }
}