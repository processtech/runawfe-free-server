package ru.runa.wfe.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import ru.runa.wfe.rest.auth.AppAuthenticationProvider;

@Configuration
@EnableWebSecurity(debug = true)
@ComponentScan(basePackageClasses = AppAuthenticationProvider.class)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String ROLE = "USER";

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
        http.authorizeRequests()
                .antMatchers(
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/test/**"
                ).permitAll()
                .antMatchers("/**").hasRole(ROLE);
        http.formLogin();
        http.httpBasic();
    }

}
