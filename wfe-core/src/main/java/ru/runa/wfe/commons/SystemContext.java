package ru.runa.wfe.commons;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Predefined context. TODO turn it on directly: 1) remove beanRefContext.xml,
 * 2) Use ru.runa.wfe.commons.SystemContextLocator in ApplicationContextFactory,
 * 3) Use SpringInterceptor
 * 
 * @author dofs
 * @since 4.0
 */
public class SystemContext extends ClassPathXmlApplicationContext {
    public static final String SYSTEM_CONTEXT_FILE_NAME = "system.context.xml";

    /**
     * Constructor to use from beanRefContext.xml
     */
    public SystemContext(String... configLocations) {
        super(configLocations, false);
        setClassLoader(ClassLoaderUtil.getExtensionClassLoader());
        refresh();
    }

    /**
     * Direct constructor
     */
    public SystemContext(boolean refresh) {
        super(new String[] { "classpath:" + SYSTEM_CONTEXT_FILE_NAME,
                "classpath*:" + SystemProperties.RESOURCE_EXTENSION_PREFIX + SYSTEM_CONTEXT_FILE_NAME }, false);
        setClassLoader(ClassLoaderUtil.getExtensionClassLoader());
        if (refresh) {
            refresh();
        }
    }

    // unused since set extension class loader in constructor (4.1.3)
    // @Override
    // public Resource[] getResources(String locationPattern) throws IOException
    // {
    // if (locationPattern.contains(SystemProperties.RESOURCE_EXTENSION_PREFIX))
    // {
    // String resourceName =
    // locationPattern.substring(locationPattern.indexOf(SystemProperties.RESOURCE_EXTENSION_PREFIX));
    // URL url = ClassLoaderUtil.getAsURL(resourceName, getClass());
    // log.info("Result of searching " + resourceName + " = " + url);
    // if (url != null) {
    // return new Resource[] { new UrlResource(url) };
    // }
    // }
    // return super.getResources(locationPattern);
    // }

}
