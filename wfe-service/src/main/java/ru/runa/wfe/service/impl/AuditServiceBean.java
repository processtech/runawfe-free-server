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
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.user.User;

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
    public byte[] getProcessHistoryDiagram(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "taskId") Long taskId, @WebParam(name = "subprocessId") String subprocessId) {
        return executionLogic.getProcessHistoryDiagram(user, processId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<NodeGraphElement> getProcessHistoryDiagramElements(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "processId") @NonNull Long processId, @WebParam(name = "taskId") Long taskId,
            @WebParam(name = "subprocessId") String subprocessId) {
        return executionLogic.getProcessHistoryDiagramElements(user, processId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public ProcessLogs getProcessLogs(@WebParam(name = "user") @NonNull User user, @WebParam(name = "filter") @NonNull ProcessLogFilter filter) {
        return auditLogic.getProcessLogs(user, filter);
    }

    @Override
    @WebResult(name = "result")
    public Object getProcessLogValue(@WebParam(name = "user") @NonNull User user, @WebParam(name = "logId") Long logId) {
        return auditLogic.getProcessLogValue(user, logId);
    }

    @Override
    @WebResult(name = "result")
    public List<SystemLog> getSystemLogs(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.SYSTEM_LOGS.createNonPaged();
        }
        return auditLogic.getSystemLogs(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public int getSystemLogsCount(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.SYSTEM_LOGS.createNonPaged();
        }
        return auditLogic.getSystemLogsCount(user, batchPresentation);
    }
}
