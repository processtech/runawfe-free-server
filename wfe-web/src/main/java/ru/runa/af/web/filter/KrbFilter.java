package ru.runa.af.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.http.AuthenticationFilter;
import ru.runa.common.web.filter.HTTPFilterBase;
import ru.runa.wfe.security.auth.KerberosLoginModuleResources;
import ru.runa.wfe.security.auth.LoginModuleConfiguration;

/**
 * This class in conjunction with {@link ru.runa.af.web.action.KrbLoginAction}
 * provides Kerberos support.
 * 
 * @web.filter name="krbfilter"
 * @web.filter-mapping url-pattern = "/krblogin.do"
 */
public class KrbFilter extends HTTPFilterBase {
    private KrbFilterConfig filterConfig;

    @Override
    public void init(FilterConfig filter) throws ServletException {
        this.filterConfig = new KrbFilterConfig(filter == null ? null : filter.getServletContext());
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!KerberosLoginModuleResources.isHttpAuthEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.length() > 33) {
                authorization = authorization.substring(0, 33) + "...";
            }
            log.debug("Request Authorization: " + authorization);
            LoginModuleConfiguration.checkThisIsDefaultConfiguration();
            AuthenticationFilter authenticationFilter = new AuthenticationFilter();
            authenticationFilter.init(filterConfig);
            authenticationFilter.doFilter(request, response, chain);
        } catch (Exception e) {
            log.error("kerberos auth", e);
            forwardToLoginPage(request, response, e);
        }
    }

}
