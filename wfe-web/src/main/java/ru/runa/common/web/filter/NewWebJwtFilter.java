package ru.runa.common.web.filter;

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
import lombok.extern.apachecommons.CommonsLog;
import org.apache.struts.Globals;
import ru.runa.common.web.Commons;
import ru.runa.wfe.security.SecuredObjectUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@CommonsLog
public class NewWebJwtFilter extends HTTPFilterBase {
	public static final String PARAMETER_NAME = "jwt";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String jwt = request.getParameter(PARAMETER_NAME);
        if (jwt != null) {
            request.getSession().setAttribute(Globals.TRANSACTION_TOKEN_KEY, jwt.substring(0, 32));
            try {
                Claims claims = getTokenClaims(jwt);
                if (isNotExpired(claims)) {
                    User user = getUser(request, claims);
                    Commons.setUser(user, request.getSession());
                }
            } catch (JwtException e) {
            	log.warn("", e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (Exception e) {
            	log.error("", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private User getUser(HttpServletRequest request, Claims claims) throws UnsupportedEncodingException {
        String login = claims.getSubject();
        Actor actor = Delegates.getExecutorService().getActorCaseInsensitive(login);
        byte[] securedKey = Base64.getDecoder().decode(claims.get("usk", String.class));
        User user = new User(actor, securedKey);
        return user;
    }

    private Claims getTokenClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SecuredObjectUtil.JWT_SECRET_KEY).build().parseClaimsJws(token).getBody();
    }

    private boolean isNotExpired(Claims claims) {
        if (claims.getExpiration().before(Calendar.getInstance().getTime())) {
            throw new JwtException("Expired token!");
        }
        return true;
    }
}
