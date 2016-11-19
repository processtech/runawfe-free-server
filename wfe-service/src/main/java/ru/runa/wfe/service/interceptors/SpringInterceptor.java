package ru.runa.wfe.service.interceptors;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.commons.SystemContextLocator;

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
