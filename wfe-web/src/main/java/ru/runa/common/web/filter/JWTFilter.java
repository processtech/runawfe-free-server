package ru.runa.common.web.filter;

import com.google.common.net.HttpHeaders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Calendar;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import ru.runa.wfe.security.SecuredObjectUtil;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@Component
public class JWTFilter extends HTTPFilterBase {
    public static final String USER_ACTOR_ID_ATTRIBUTE_NAME = "uid";
    public static final String USER_SECURED_KEY_ATTRIBUTE_NAME = "usk";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith(SecuredObjectUtil.BEARER_PREFIX)) {
            try {
                Claims claims = getTokenClaims(token);
                if (isNotExpired(claims)) {
                    User user = getUser(request, claims);
                    request.setAttribute("user", user);
                }
            } catch (JwtException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private User getUser(HttpServletRequest request, Claims claims) throws UnsupportedEncodingException {
        String login = claims.getSubject();
        Actor actor = Delegates.getExecutorService().getActorCaseInsensitive(login);
        byte[] securedKey = Base64.getDecoder().decode(claims.get(USER_SECURED_KEY_ATTRIBUTE_NAME, String.class));
        User user = new User(actor, securedKey);
        return user;
    }

    private Claims getTokenClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SecuredObjectUtil.JWT_SECRET_KEY).build().parseClaimsJws(token.replace(SecuredObjectUtil.BEARER_PREFIX, "")).getBody();
    }

    private boolean isNotExpired(Claims claims) {
        if (claims.getExpiration().before(Calendar.getInstance().getTime())) {
            throw new JwtException("Exired token!");
        }
        return true;
    }
}
