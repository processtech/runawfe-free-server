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
package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.User;

/**
 * Audit service.
 * 
 * @author Dofs
 * @since 4.2.0
 */
public interface AuditService {

    /**
     * Gets process graph element for history diagram.
     * 
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param taskId
     *            active task id
     * @param subprocessId
     *            embedded subprocess id, can be <code>null</code>
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public List<GraphElementPresentation> getProcessHistoryDiagramElements(User user, Long processId, Long taskId, String subprocessId)
            throws ProcessDoesNotExistException;

    /**
     * Gets process history graphical diagram PNG image.
     * 
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param taskId
     *            task id
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId, String subprocessId) throws ProcessDoesNotExistException;

    /**
     * Gets process logs by filter.
     * 
     * @param user
     *            authorized user
     * @param filter
     *            process log filter
     * @return not <code>null</code>
     */
    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter);

    /**
     * Gets process log byte array value.
     * 
     * @param user
     *            authorized user
     * @param logId
     *            process log id
     * @return value or <code>null</code>
     */
    public Object getProcessLogValue(User user, Long logId);

    /**
     * Gets system logs for {@link BatchPresentation}.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation);

    /**
     * Gets system log count for {@link BatchPresentation}.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation);
    
    /**
     * Gets the latest task assign log entity by the given task id
     * 
     * @param user
     *            authorized user
     * @param processId
     * 			  process ID
     * @param taskId
     * 			  task ID
     * @return task log entity
     */
    public ProcessLog getLatestAssignTaskLog(User user, Long processId, Long taskId);
}
