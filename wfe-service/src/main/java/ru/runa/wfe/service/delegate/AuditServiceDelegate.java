package ru.runa.wfe.service.delegate;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.AuditService;
import ru.runa.wfe.user.User;

/**
 * Created on 28.09.2004
 */
public class AuditServiceDelegate extends Ejb3Delegate implements AuditService {

    public AuditServiceDelegate() {
        super(AuditService.class);
    }

    private AuditService getAuditService() {
        return getService();
    }

    @Override
    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId, String subprocessId) {
        try {
            return getAuditService().getProcessHistoryDiagram(user, processId, taskId, subprocessId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, Long taskId, String subprocessId) {
        try {
            return getAuditService().getProcessHistoryDiagramElements(user, processId, taskId, subprocessId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        try {
            return getAuditService().getProcessLogs(user, filter);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        try {
            return getAuditService().getSystemLogs(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        try {
            return getAuditService().getSystemLogsCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void cleanProcessLogsBeforeDate(User user, Date date) {
        try {
            getAuditService().cleanProcessLogsBeforeDate(user, date);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Object getProcessLogValue(User user, Long logId) {
        try {
            return getAuditService().getProcessLogValue(user, logId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
