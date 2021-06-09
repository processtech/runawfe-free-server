package ru.runa.wfe.rest.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/auth")
@Transactional
@CommonsLog
public class AuthController {
    @Autowired
    private AuthenticationLogic authenticationLogic;

    @PostMapping("/token")
    public String token(@RequestParam String login, @RequestParam String password) {
        User user = authenticationLogic.authenticate(login, password);
        Instant expirationInstant = Instant.now().plus(1, ChronoUnit.DAYS);
        String token = Jwts
                .builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(login)
                .claim(JwtAuthenticationFilter.USER_ACTOR_ID_ATTRIBUTE_NAME, user.getActor().getId())
                .claim(JwtAuthenticationFilter.USER_SECURED_KEY_ATTRIBUTE_NAME, Base64.getEncoder().encode(user.getSecuredKey()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(expirationInstant))
                .signWith(JwtAuthenticationFilter.JWT_SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
        return JwtAuthenticationFilter.BEARER_PREFIX + token;
    }

    @PostMapping("/kerberos")
    public User authenticateByKerberos(@RequestParam byte[] token) {
        log.debug("Authenticating (kerberos)");
        User user = authenticationLogic.authenticate(token);
        log.debug("Authenticated (kerberos): " + user);
        return user;
    }

    @PostMapping("/loginPassword")
    public User authenticateByLoginPassword(@RequestParam String login, @RequestParam String password) {
        log.debug("Authenticating (login) " + login);
        User user = authenticationLogic.authenticate(login, password);
        log.debug("Authenticated (login): " + user);
        return user;
    }

    @PostMapping("/trustedPrincipal")
    public User authenticateByTrustedPrincipal(@AuthenticationPrincipal AuthUser authUser, @RequestParam String login) {
        log.debug("Authenticating (trusted) " + login);
        User user = authenticationLogic.authenticate(authUser.getUser(), login);
        log.debug("Authenticated (trusted): " + user);
        return user;
    }

    // Тестовый запрос для проверки авторизации по токену
    @PostMapping("/validate")
    public String validate(@RequestParam String token) {
        return token;
    }
}