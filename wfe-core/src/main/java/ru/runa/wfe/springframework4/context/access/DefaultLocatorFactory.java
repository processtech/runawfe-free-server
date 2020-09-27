package ru.runa.wfe.springframework4.context.access;

import org.springframework.beans.FatalBeanException;
import ru.runa.wfe.springframework4.beans.factory.access.BeanFactoryLocator;

/**
 * A factory class to get a default ContextSingletonBeanFactoryLocator instance.
 *
 * @author Colin Sampaleanu
 * @see org.springframework.context.access.ContextSingletonBeanFactoryLocator
 */
public class DefaultLocatorFactory {

    /**
     * Return an instance object implementing BeanFactoryLocator. This will normally be a singleton instance of the specific
     * ContextSingletonBeanFactoryLocator class, using the default resource selector.
     */
    public static BeanFactoryLocator getInstance() throws FatalBeanException {
        return ContextSingletonBeanFactoryLocator.getInstance();
    }

    /**
     * Return an instance object implementing BeanFactoryLocator. This will normally be a singleton instance of the specific
     * ContextSingletonBeanFactoryLocator class, using the specified resource selector.
     * 
     * @param selector
     *            a selector variable which provides a hint to the factory as to which instance to return.
     */
    public static BeanFactoryLocator getInstance(String selector) throws FatalBeanException {
        return ContextSingletonBeanFactoryLocator.getInstance(selector);
    }
}
