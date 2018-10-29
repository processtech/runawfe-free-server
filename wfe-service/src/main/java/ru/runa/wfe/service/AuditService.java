package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.graph.view.NodeGraphElement;
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
    List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, Long taskId, String subprocessId)
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
    byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId, String subprocessId) throws ProcessDoesNotExistException;

    /**
     * Gets process logs by filter.
     * 
     * @param user
     *            authorized user
     * @param filter
     *            process log filter
     * @return not <code>null</code>
     */
    ProcessLogs getProcessLogs(User user, ProcessLogFilter filter);

    /**
     * Gets process log byte array value.
     * 
     * @param user
     *            authorized user
     * @param logId
     *            process log id
     * @return value or <code>null</code>
     */
    Object getProcessLogValue(User user, Long logId);

    /**
     * Gets system logs for {@link BatchPresentation}.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation);

    /**
     * Gets system log count for {@link BatchPresentation}.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    int getSystemLogsCount(User user, BatchPresentation batchPresentation);
}
