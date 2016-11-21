package ru.runa.af.web.filter;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import ru.runa.wfe.security.auth.KerberosLoginModuleResources;

public class KrbFilterConfig implements FilterConfig {
    private String filterName = "krbfilter";
    private Hashtable<String, String> initParams = new Hashtable<String, String>();
    private final ServletContext context;

    public KrbFilterConfig(ServletContext context) {
        this.context = context;
        if (KerberosLoginModuleResources.isHttpAuthEnabled()) {
            initParams.putAll(KerberosLoginModuleResources.getInitParameters());
        }
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public String getInitParameter(String key) {
        return initParams.get(key);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return initParams.keys();
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

}
