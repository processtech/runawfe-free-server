package ru.runa.wfe.job.dao;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.TimerJob;

/**
 * DAO for {@link Job} hierarchy.
 * 
 * @author dofs
 * @since 4.0
 */
public class JobDAO extends GenericDAO<Job> {

    public List<Job> getExpiredJobs() {
        return getHibernateTemplate().find("from Job where dueDate<=? and token.executionStatus=? order by dueDate", new Date(),
                ExecutionStatus.ACTIVE);
    }

    public List<Job> findByProcess(Process process) {
        return getHibernateTemplate().find("from Job where process=? order by dueDate", process);
    }

    public List<Job> findByProcessAndDeadlineExpressionContaining(Process process, String expression) {
        return getHibernateTemplate().find("from Job where process=? and dueDateExpression like ?", process, "%" + expression + "%");
    }

    public void deleteTimersByName(Token token, String name) {
        log.debug("deleting timers by name '" + name + "' for " + token);
        List<TimerJob> timerJobs = getHibernateTemplate().find("from TimerJob where token=? and name=?", token, name);
        getHibernateTemplate().deleteAll(timerJobs);
        log.debug(timerJobs.size() + " timers by name '" + name + "' for " + token + " were deleted");
    }

    public void deleteAll(Process process) {
        log.debug("deleting jobs for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Job where process=?", process);
    }

}
