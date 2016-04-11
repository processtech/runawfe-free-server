package ru.runa.wfe.service.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.service.ArchivingService;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.service.logic.archiving.ArchivingLogic;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ArchivingAPI", serviceName = "ArchivingWebService")
@SOAPBinding
public class ArchivingServiceBean implements ArchivingService {

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ArchivingLogic archivingLogic;

    @Override
    @WebResult(name = "result")
    public void backupProcess(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processId != null);
        archivingLogic.backupProcess(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public void backupProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName,
            @WebParam(name = "version") Long version) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionName != null);
        Preconditions.checkArgument(version != null);
        archivingLogic.backupProcessDefinition(user, definitionName, version);
    }

    @Override
    @WebResult(name = "result")
    public void restoreProcess(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processId != null);
        archivingLogic.restoreProcess(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public void restoreProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName,
            @WebParam(name = "version") Long version) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionName != null);
        Preconditions.checkArgument(version != null);
        archivingLogic.restoreProcessDefinition(user, definitionName, version);
    }

}
