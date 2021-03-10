package ru.runa.wfe.rest.config;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;
import ru.runa.wfe.commons.ApplicationContextFactory;

public class SpringWebMvcDispatcherServletInitializer extends AbstractDispatcherServletInitializer {

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/*" };
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        return context;
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        if (ApplicationContextFactory.getContext() == null) {
            throw new RuntimeException("SystemContext is not initialized yet :(");
        }
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setParent(ApplicationContextFactory.getContext());
        context.register(AppConfig.class);
        context.register(SpringSecurityConfig.class);
        return context;
    }

}
