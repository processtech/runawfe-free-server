package ru.runa.wfe.service.impl;

import java.util.ArrayList;
import java.util.Collections;
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
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.service.decl.SystemServiceLocal;
import ru.runa.wfe.service.decl.SystemServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
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
    public List<ProcessError> getAllProcessErrors(@NonNull User user) {
        val result = new ArrayList<ProcessError>();
        for (List<ProcessError> cached : Errors.getProcessErrors().values()) {
            result.addAll(cached);
        }
        List<WfProcess> processes = executionLogic.getFailedProcesses(user);
        for (WfProcess process : processes) {
            populateExecutionErrors(user, result, process.getId());
        }
        Collections.sort(result);
        return result;
    }

    @Override
    @WebResult(name = "result")
    public List<ProcessError> getProcessErrors(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        val result = new ArrayList<ProcessError>();
        List<ProcessError> cached = Errors.getProcessErrors(processId);
        if (cached != null) {
            result.addAll(cached);
        }
        populateExecutionErrors(user, result, processId);
        Collections.sort(result);
        return result;
    }

    @Override
    @WebResult(name = "result")
    public List<SystemError> getSystemErrors(@WebParam(name = "user") @NonNull User user) {
        val result = new ArrayList<SystemError>(Errors.getSystemErrors());
        Collections.sort(result);
        return result;
    }

    private void populateExecutionErrors(User user, List<ProcessError> list, Long processId) {
        try {
            for (WfToken token : executionLogic.getTokens(user, processId, false, true)) {
                if (token.getExecutionStatus() != ExecutionStatus.FAILED) {
                    continue;
                }
                if (token.getErrorMessage() == null) {
                    // during feature integration
                    continue;
                }
                ProcessError processError = new ProcessError(ProcessErrorType.execution, processId, token.getNode().getId());
                processError.setNodeName(token.getNode().getName());
                processError.setMessage(token.getErrorMessage());
                processError.setOccurredDate(token.getErrorDate());
                list.add(processError);
            }
        } catch (Exception e) {
            log.warn("Unable to populate errors in process " + processId, e);
            ProcessError processError = new ProcessError(ProcessErrorType.execution, processId, "");
            processError.setMessage("Unable to populate errors in this process");
            list.add(processError);
        }
    }
}
