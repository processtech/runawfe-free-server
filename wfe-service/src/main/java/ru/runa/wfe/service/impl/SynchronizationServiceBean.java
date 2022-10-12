package ru.runa.wfe.service.impl;

import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.cache.CacheFreezingExecutor;
import ru.runa.wfe.security.logic.LdapLogic;
import ru.runa.wfe.service.SynchronizationService;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.user.User;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ SpringBeanAutowiringInterceptor.class, EjbExceptionSupport.class, PerformanceObserver.class,
        EjbTransactionSupport.class })
public class SynchronizationServiceBean implements SynchronizationService {
    @Autowired
    private LdapLogic ldapLogic;

    @Override
    public int synchronizeExecutorsWithLdap(@NonNull User user) {
        final AtomicInteger result = new AtomicInteger();
        new CacheFreezingExecutor() {

            @Override
            protected void doExecute() {
                result.set(ldapLogic.synchronizeExecutors());
            }

        }.execute();
        return result.get();
    }

}
