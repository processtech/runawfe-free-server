package ru.runa.wfe.job.dao;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.Timer;

/**
 * DAO for {@link Job} hierarchy.
 *
 * @author dofs
 * @since 4.0
 */
public class JobDAO extends GenericDAO<Job> {

    public List<Job> getExpiredJobs() {
        if (SystemProperties.isProcessSuspensionBlocksProcessExecution()) {
            return getHibernateTemplate().find("from Job where dueDate<=? and process.executionStatus=? order by dueDate", new Date(),
                    ExecutionStatus.ACTIVE);
        }
        return getHibernateTemplate().find("from Job where dueDate<=? order by dueDate", new Date());
    }

    public List<Job> findByProcess(Process process) {
        return getHibernateTemplate().find("from Job where process=? order by dueDate", process);
    }

    public List<Job> findByProcessAndDeadlineExpressionContaining(Process process, String expression) {
        return getHibernateTemplate().find("from Job where process=? and dueDateExpression like ?", process, "%" + expression + "%");
    }

    public void deleteTimersByName(String name, Token token) {
        log.debug("deleting timers by name '" + name + "' for " + token);
        List<Timer> timers = getHibernateTemplate().find("from Timer where token=? and name=?", token, name);
        getHibernateTemplate().deleteAll(timers);
        log.debug(timers.size() + " timers by name '" + name + "' for " + token + " were deleted");
    }

    public void deleteAll(Process process) {
        log.debug("deleting jobs for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Job where process=?", process);
    }

}
