package ru.runa.wfe.job.dao;

import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.QJob;
import ru.runa.wfe.job.QTimerJob;

/**
 * DAO for {@link Job} hierarchy.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class JobDao extends GenericDao<Job> {

    public JobDao() {
        super(Job.class);
    }

    public List<Job> getExpiredJobs(Long limit) {
        QJob j = QJob.job;
        return queryFactory.selectFrom(j)
                .where(j.dueDate.loe(new Date()).and(j.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(j.token.endDate.isNull()))
                .orderBy(j.dueDate.asc())
                .limit(limit)
                .fetch();
    }

    public Long getExpiredJobsCount() {
        QJob j = QJob.job;
        return queryFactory.selectFrom(j)
                .where(j.dueDate.loe(new Date()).and(j.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(j.token.endDate.isNull()))
                .fetchCount();
    }

    public List<Job> findByProcess(CurrentProcess process) {
        QJob j = QJob.job;
        return queryFactory.selectFrom(j).where(j.process.eq(process)).orderBy(j.dueDate.asc()).fetch();
    }

    public List<Job> findByProcessAndDeadlineExpressionContaining(CurrentProcess process, String expression) {
        val j = QJob.job;
        return queryFactory.selectFrom(j).where(j.process.eq(process).and(j.dueDateExpression.like("%" + expression + "%"))).fetch();
    }

    public void deleteByToken(CurrentToken token) {
        val tj = QTimerJob.timerJob;
        queryFactory.delete(tj).where(tj.token.eq(token)).execute();
    }

    public void deleteByProcess(CurrentProcess process) {
        log.debug("deleting jobs for process " + process.getId());
        val j = QJob.job;
        queryFactory.delete(j).where(j.process.eq(process)).execute();
    }
}
