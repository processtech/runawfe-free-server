package ru.runa.wfe.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.security.SecuredObjectUtil;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
@CommonsLog
public class JwtUser {
    public static final String USER_ACTOR_ID_ATTRIBUTE_NAME = "uid";
    public static final String USER_SECURED_KEY_ATTRIBUTE_NAME = "usk";

    public User with(String token) throws JwtException {
        final Claims claims = Jwts.parserBuilder().setSigningKey(SecuredObjectUtil.JWT_SECRET_KEY).build().parseClaimsJws(token).getBody();

        final long actorId = ((Number) claims.get(USER_ACTOR_ID_ATTRIBUTE_NAME)).longValue();
        final byte[] securedKey = Base64.getDecoder().decode((String) claims.get(USER_SECURED_KEY_ATTRIBUTE_NAME));
        final Actor actor = new Actor(claims.getSubject(), "");
        actor.setId(actorId);

        final User user = new User(actor, securedKey);
        SubjectPrincipalsHelper.validateUser(user);
        return user;
    }

    public String tokenOf(User user) {
        final Instant expirationInstant = Instant.now().plus(1, ChronoUnit.DAYS);
        return Jwts
                .builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getName())
                .claim(JwtUser.USER_ACTOR_ID_ATTRIBUTE_NAME, user.getActor().getId())
                .claim(JwtUser.USER_SECURED_KEY_ATTRIBUTE_NAME, Base64.getEncoder().encodeToString(user.getSecuredKey()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(expirationInstant))
                .signWith(SecuredObjectUtil.JWT_SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }
}
