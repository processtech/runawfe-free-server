package ru.runa.wfe.commons;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Code copied from
 * org.springframework.context.access.ContextSingletonBeanFactoryLocator
 * 
 * @author dofs
 * @since 4.0.5
 */
public class SystemContextLocator extends SingletonBeanFactoryLocator {

    private static volatile BeanFactoryLocator beanFactoryLocator;

    private SystemContextLocator() {
        super("system.context");
    }

    public static BeanFactoryLocator getInstance() throws BeansException {
        if (beanFactoryLocator == null) {
            synchronized (SystemContextLocator.class) {
                beanFactoryLocator = new SystemContextLocator();
            }
        }
        return beanFactoryLocator;
    }

    @Override
    protected BeanFactory createDefinition(String resourceLocation, String factoryKey) {
        return new SystemContext(false);
    }

    @Override
    protected void initializeDefinition(BeanFactory groupDef) {
        ((ConfigurableApplicationContext) groupDef).refresh();
    }

    @Override
    protected void destroyDefinition(BeanFactory groupDef, String selector) {
        ((ConfigurableApplicationContext) groupDef).close();
    }

}
