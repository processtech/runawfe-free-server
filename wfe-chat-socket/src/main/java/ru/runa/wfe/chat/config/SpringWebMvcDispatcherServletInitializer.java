package ru.runa.wfe.chat.config;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;
import ru.runa.wfe.commons.ClassLoaderUtil;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
public class SpringWebMvcDispatcherServletInitializer extends AbstractDispatcherServletInitializer {

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/*"};
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setClassLoader(new CombinedClassLoader(ClassLoaderUtil.getExtensionClassLoader()));
        context.register(PropertiesConfig.class, ChatConfig.class, WebSocketConfig.class);
        return context;
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        return createServletApplicationContext();
    }

}
