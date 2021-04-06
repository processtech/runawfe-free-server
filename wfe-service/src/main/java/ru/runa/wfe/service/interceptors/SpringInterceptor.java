package ru.runa.wfe.service.interceptors;

import ru.runa.wfe.commons.SystemContextLocator;
import ru.runa.wfe.springframework4.beans.factory.access.BeanFactoryLocator;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 * @author dofs
 * @since 4.0.5
 */
public class SpringInterceptor extends SpringBeanAutowiringInterceptor {
    @Override
    protected BeanFactoryLocator getBeanFactoryLocator(Object target) {
        return SystemContextLocator.getInstance();
    }
}
