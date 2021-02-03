package ru.runa.wfe.rest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;
import ru.runa.wfe.commons.ApplicationContextFactory;

public class ApiServletInitializer extends AbstractDispatcherServletInitializer {

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/*" };
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(ApiConfig.class);
        return context;
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        if (ApplicationContextFactory.getContext() == null) {
            throw new RuntimeException("SystemContext is not initialized yet :(");
        }
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setParent(ApplicationContextFactory.getContext());
        return context;
    }

}
