package ru.runa.wfe.rest.auth;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.rest.config.SpringSecurityConfig;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.user.User;

@Component
@Transactional
public class AppAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AuthenticationLogic authenticationLogic;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User user = authenticationLogic.authenticate(authentication.getName(), (String) authentication.getCredentials());
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + SpringSecurityConfig.ROLE));
        return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
