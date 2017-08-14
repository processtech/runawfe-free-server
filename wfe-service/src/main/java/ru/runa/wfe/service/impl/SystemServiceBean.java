/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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

import org.jfree.util.Log;
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
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.service.decl.SystemServiceLocal;
import ru.runa.wfe.service.decl.SystemServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Represent system ru.runa.commons.test operations login/logout. Created on 16.08.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "SystemAPI", serviceName = "SystemWebService")
@SOAPBinding
public class SystemServiceBean implements SystemServiceLocal, SystemServiceRemote {
    @Autowired
    private AuditLogic auditLogic;
    @Autowired
    private ExecutionLogic executionLogic;

    @Override
    @WebResult(name = "result")
    public void login(@WebParam(name = "user") User user) {
        Preconditions.checkArgument(user != null, "user");
        auditLogic.login(user, ASystem.INSTANCE);
    }

    @Override
    @WebResult(name = "result")
    public List<Localization> getLocalizations(@WebParam(name = "user") User user) {
        Preconditions.checkArgument(user != null, "user");
        return auditLogic.getLocalizations(user);
    }

    @Override
    @WebResult(name = "result")
    public String getLocalized(@WebParam(name = "user") User user, @WebParam(name = "name") String name) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(name != null, "name");
        return auditLogic.getLocalized(user, name);
    }

    @Override
    @WebResult(name = "result")
    public void saveLocalizations(@WebParam(name = "user") User user, @WebParam(name = "localizations") List<Localization> localizations) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(localizations != null, "localizations");
        auditLogic.saveLocalizations(user, localizations);
    }

    @Override
    @WebResult(name = "result")
    public String getSetting(@WebParam(name = "fileName") String fileName, @WebParam(name = "name") String name) {
        Preconditions.checkArgument(fileName != null, "fileName");
        Preconditions.checkArgument(name != null, "name");
        return auditLogic.getSetting(fileName, name);
    }

    @Override
    @WebResult(name = "result")
    public void setSetting(@WebParam(name = "fileName") String fileName, @WebParam(name = "name") String name, @WebParam(name = "value") String value) {
        Preconditions.checkArgument(fileName != null, "fileName");
        Preconditions.checkArgument(name != null, "name");
        auditLogic.setSetting(fileName, name, value);
    }

    @Override
    @WebResult(name = "result")
    public void clearSettings() {
        auditLogic.clearSettings();
    }

    @Override
    @WebMethod(exclude = true)
    public List<ProcessError> getAllProcessErrors(User user) {
        Preconditions.checkArgument(user != null, "user");
        List<ProcessError> result = Lists.newArrayList();
        for (List<ProcessError> list : Errors.getProcessErrors().values()) {
            result.addAll(list);
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
    public List<ProcessError> getProcessErrors(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(processId != null, "processId");
        List<ProcessError> list = new ArrayList<ProcessError>();
        List<ProcessError> cached = Errors.getProcessErrors(processId);
        if (cached != null) {
            list.addAll(cached);
        }
        populateExecutionErrors(user, list, processId);
        Collections.sort(list);
        return list;
    }

    @Override
    @WebResult(name = "result")
    public List<SystemError> getSystemErrors(@WebParam(name = "user") User user) {
        Preconditions.checkArgument(user != null, "user");
        List<SystemError> list = Lists.newArrayList(Errors.getSystemErrors());
        Collections.sort(list);
        return list;
    }

    private void populateExecutionErrors(User user, List<ProcessError> list, Long processId) {
        try {
            for (WfToken token : executionLogic.getTokens(user, processId, false)) {
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
            Log.warn("Unable to populate errors in process " + processId, e);
            ProcessError processError = new ProcessError(ProcessErrorType.execution, processId, "");
            processError.setMessage("Unable to populate errors in this process");
            list.add(processError);
        }
    }
}
