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

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.AuditServiceLocal;
import ru.runa.wfe.service.decl.AuditServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

@Stateless(name = "AuditServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AuditAPI", serviceName = "AuditWebService")
@SOAPBinding
public class AuditServiceBean implements AuditServiceLocal, AuditServiceRemote {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private AuditLogic auditLogic;

    @Override
    @WebResult(name = "result")
    public byte[] getProcessHistoryDiagram(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "taskId") Long taskId, @WebParam(name = "subprocessId") String subprocessId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(processId != null, "processId");
        return executionLogic.getProcessHistoryDiagram(user, processId, taskId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<NodeGraphElement> getProcessHistoryDiagramElements(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "taskId") Long taskId, @WebParam(name = "subprocessId") String subprocessId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(processId != null, "processId");
        return executionLogic.getProcessHistoryDiagramElements(user, processId, taskId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public ProcessLogs getProcessLogs(@WebParam(name = "user") User user, @WebParam(name = "filter") ProcessLogFilter filter) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(filter != null, "filter");
        return auditLogic.getProcessLogs(user, filter);
    }

    @Override
    @WebResult(name = "result")
    public Object getProcessLogValue(@WebParam(name = "user") User user, @WebParam(name = "logId") Long logId) {
        Preconditions.checkArgument(user != null, "user");
        return auditLogic.getProcessLogValue(user, logId);
    }

    @Override
    @WebResult(name = "result")
    public List<SystemLog> getSystemLogs(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.SYSTEM_LOGS.createNonPaged();
        }
        return auditLogic.getSystemLogs(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public int getSystemLogsCount(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.SYSTEM_LOGS.createNonPaged();
        }
        return auditLogic.getSystemLogsCount(user, batchPresentation);
    }
}
