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
package ru.runa.wfe.audit.logic;

import com.google.common.base.Preconditions;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.dao.ProcessLogDao2;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.execution.BaseProcess;
import ru.runa.wfe.execution.dao.NodeProcessDao2;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;

/**
 * Audit logic.
 * 
 * @author dofs
 * @since 4.0
 */
public class AuditLogic extends CommonLogic {
    @Autowired
    private ProcessLogDao2 processLogDao2;
    @Autowired
    private NodeProcessDao2 nodeProcessDao2;

    public void login(User user) {
        permissionDao.checkAllowed(user, Permission.LOGIN, SecuredSingleton.EXECUTORS);
    }

    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        Preconditions.checkNotNull(filter.getProcessId(), "filter.processId");
        BaseProcess process = processDao.getNotNull(filter.getProcessId());
        permissionDao.checkAllowed(user, Permission.LIST, process);
        ProcessLogs result = new ProcessLogs(filter.getProcessId());
        List<BaseProcessLog> logs = processLogDao2.getAll(filter);
        result.addLogs(logs, filter.isIncludeSubprocessLogs());
        if (filter.isIncludeSubprocessLogs()) {
            for (BaseProcess subprocess : nodeProcessDao2.getSubprocessesRecursive(process)) {
                ProcessLogFilter subprocessFilter = new ProcessLogFilter(subprocess.getId());
                subprocessFilter.setSeverities(filter.getSeverities());
                logs = processLogDao2.getAll(subprocessFilter);
                result.addLogs(logs, filter.isIncludeSubprocessLogs());
            }
        }
        return result;
    }

    public Object getProcessLogValue(User user, Long logId) {
        Preconditions.checkNotNull(logId, "logId");
        IProcessLog processLog = processLogDao2.getNotNull(logId);
        permissionDao.checkAllowed(user, Permission.LIST, SecuredObjectType.PROCESS, processLog.getProcessId());
        return processLog.getBytes();
    }

    /**
     * Load system logs according to {@link BatchPresentation}.
     * 
     * @param user
     *            Requester user.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs.
     * @return Loaded system logs.
     */
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        permissionDao.checkAllowed(user, Permission.LIST, SecuredSingleton.LOGS);
        PresentationConfiguredCompiler<SystemLog> compiler = PresentationCompilerHelper.createAllSystemLogsCompiler(user, batchPresentation);
        return compiler.getBatch();
    }

    /**
     * Load system logs count according to {@link BatchPresentation}.
     * 
     * @param user
     *            Requester user.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs count.
     * @return System logs count.
     */
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        permissionDao.checkAllowed(user, Permission.LIST, SecuredSingleton.LOGS);
        PresentationConfiguredCompiler<SystemLog> compiler = PresentationCompilerHelper.createAllSystemLogsCompiler(user, batchPresentation);
        return compiler.getCount();
    }
}
