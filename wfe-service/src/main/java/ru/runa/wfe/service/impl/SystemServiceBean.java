package ru.runa.wfe.service.impl;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.commons.SystemErrors;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.SystemServiceLocal;
import ru.runa.wfe.service.decl.SystemServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.user.User;

/**
 * Represent system ru.runa.commons.test operations login/logout. Created on 16.08.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "SystemAPI", serviceName = "SystemWebService")
@SOAPBinding
@CommonsLog
public class SystemServiceBean implements SystemServiceLocal, SystemServiceRemote {
    @Autowired
    private AuditLogic auditLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private CommonLogic commonLogic;

    @Override
    public void initialize() {
        // interceptors are invoked
    }

    @Override
    @WebResult(name = "result")
    public void login(@WebParam(name = "user") @NonNull User user) {
        auditLogic.login(user);
    }

    @Override
    @WebResult(name = "result")
    public List<Localization> getLocalizations() {
        return auditLogic.getLocalizations();
    }

    @Override
    @WebResult(name = "result")
    public String getLocalized(@WebParam(name = "name") @NonNull String name) {
        return auditLogic.getLocalized(name);
    }

    @Override
    @WebResult(name = "result")
    public void saveLocalizations(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "localizations") @NonNull List<Localization> localizations) {
        auditLogic.saveLocalizations(user, localizations);
    }

    @Override
    @WebResult(name = "result")
    public String getSetting(@WebParam(name = "fileName") @NonNull String fileName, @WebParam(name = "name") @NonNull String name) {
        return auditLogic.getSetting(fileName, name);
    }

    @Override
    @WebResult(name = "result")
    public void setSetting(@WebParam(name = "fileName") @NonNull String fileName, @WebParam(name = "name") @NonNull String name,
            @WebParam(name = "value") String value) {
        auditLogic.setSetting(fileName, name, value);
    }

    @Override
    @WebResult(name = "result")
    public void clearSettings() {
        auditLogic.clearSettings();
    }

    @Override
    @WebMethod(exclude = true)
    public void failToken(User user, Long tokenId, String errorMessage, String stackTrace) {
        executionLogic.failToken(tokenId, errorMessage, stackTrace);
    }

    @Override
    @WebMethod(exclude = true)
    public void removeTokenError(User user, Long tokenId) {
        executionLogic.removeTokenError(tokenId);
    }

    @Override
    @WebMethod(exclude = true)
    public List<WfTokenError> getTokenErrors(@NonNull User user, BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TOKEN_ERRORS.createDefault();
        }
        return executionLogic.getTokenErrors(user, batchPresentation);
    }

    @Override
    @WebMethod(exclude = true)
    public int getTokenErrorsCount(User user, BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.TOKEN_ERRORS.createDefault();
        }
        return executionLogic.getTokenErrorsCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public List<WfTokenError> getTokenErrorsByProcessId(@NonNull User user, @NonNull Long processId) {
        return executionLogic.getTokenErrors(user, processId);
    }

    @Override
    public WfTokenError getTokenError(User user, Long tokenId) {
        return executionLogic.getTokenError(user, tokenId);
    }

    @Override
    @WebResult(name = "result")
    public List<SystemError> getSystemErrors(@WebParam(name = "user") @NonNull User user) {
        return SystemErrors.getErrors();
    }

    @Override
    @WebResult(name = "result")
    public byte[] exportDataFile(User user) {
        return commonLogic.exportDataFile(user);
    }

    @Override
    @WebResult(name = "result")
    public boolean isPasswordCheckRequired(){
        return commonLogic.isPasswordCheckRequired();
    }

}
