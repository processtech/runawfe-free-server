package ru.runa.wfe.rest.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.auth.JwtUser;
import ru.runa.wfe.rest.dto.WfeCredentials;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/auth")
@Transactional
public class AuthController {
    @Autowired
    private AuthenticationLogic authenticationLogic;

    @PostMapping("/basic")
    public String basic(@RequestBody WfeCredentials request) {
        User user = authenticationLogic.authenticate(request.getLogin(), request.getPassword());
        return new JwtUser().tokenOf(user);
    }

    @PostMapping("/kerberos")
    public String kerberos(@RequestBody byte[] token) {
        User user = authenticationLogic.authenticate(token);
        return new JwtUser().tokenOf(user);
    }

    @PostMapping("/trusted")
    public String trusted(@AuthenticationPrincipal AuthUser authUser, @RequestBody String login) {
        User user = authenticationLogic.authenticate(authUser.getUser(), login);
        return new JwtUser().tokenOf(user);
    }

    // TODO 2678 remove
    @PostMapping("/validate")
    public String validate(@RequestParam String token) {
        return token;
    }
    
}