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
package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.audit.ProcessLog;
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
public class AuditServiceDelegate extends EJB3Delegate implements AuditService {

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
    public Object getProcessLogValue(User user, Long logId) {
        try {
            return getAuditService().getProcessLogValue(user, logId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
    @Override
    public ProcessLog getLatestAssignTaskLog(User user, Long processId, Long taskId) {
        try {
            return getAuditService().getLatestAssignTaskLog(user, processId, taskId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
