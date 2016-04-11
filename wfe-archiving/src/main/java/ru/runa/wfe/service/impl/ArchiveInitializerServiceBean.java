package ru.runa.wfe.service.impl;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.UserTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.service.ArchiveInitializerService;
import ru.runa.wfe.service.interceptors.CacheNotifier;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.logic.archiving.ArchivingInitializerLogic;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ CacheNotifier.class, EjbExceptionSupport.class, SpringBeanAutowiringInterceptor.class })
public class ArchiveInitializerServiceBean implements ArchiveInitializerService {

    @Resource
    private SessionContext sessionContext;
    @Autowired
    private ArchivingInitializerLogic archivingInitializerLogic;

    @Override
    public void onSystemStartup() {
        UserTransaction transaction = sessionContext.getUserTransaction();
        archivingInitializerLogic.onStartup(transaction);
    }
}
