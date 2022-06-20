package ru.runa.wfe.rest.auth;

import ru.runa.wfe.security.SecuredObjectUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
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
public class AuthController {
    @Autowired
    private AuthenticationLogic authenticationLogic;

    @PostMapping("/basic")
    public String basic(@RequestParam String login, @RequestParam String password) {
        User user = authenticationLogic.authenticate(login, password);
        return tokenOf(user);
    }

    @PostMapping("/kerberos")
    public String kerberos(@RequestParam byte[] token) {
        User user = authenticationLogic.authenticate(token);
        return tokenOf(user);
    }

    @PostMapping("/trusted")
    public String trusted(@AuthenticationPrincipal AuthUser authUser, @RequestParam String login) {
        User user = authenticationLogic.authenticate(authUser.getUser(), login);
        return tokenOf(user);
    }

    // TODO 2678 remove
    @PostMapping("/validate")
    public String validate(@RequestParam String token) {
        return token;
    }
    
    private String tokenOf(User user) {
        Instant expirationInstant = Instant.now().plus(1, ChronoUnit.DAYS);
        String token = Jwts
                .builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getName())
                .claim(JwtAuthenticationFilter.USER_ACTOR_ID_ATTRIBUTE_NAME, user.getActor().getId())
                .claim(JwtAuthenticationFilter.USER_SECURED_KEY_ATTRIBUTE_NAME, Base64.getEncoder().encodeToString(user.getSecuredKey()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(expirationInstant))
                .signWith(SecuredObjectUtil.JWT_SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
        return token;
    }
}