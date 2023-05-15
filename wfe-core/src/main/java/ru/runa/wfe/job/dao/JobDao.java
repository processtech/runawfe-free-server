package ru.runa.wfe.job.dao;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
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

    public List<Long> getExpiredJobIds(Long limit) {
        QJob j = QJob.job;
        return queryFactory.select(j.id).from(j)
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

    public List<Job> findByProcess(Process process) {
        QJob j = QJob.job;
        return queryFactory.selectFrom(j).where(j.process.eq(process)).orderBy(j.dueDate.asc()).fetch();
    }

    public List<Job> findByProcessAndDeadlineExpressionContaining(Process process, String expression) {
        QJob j = QJob.job;
        return queryFactory.selectFrom(j).where(j.process.eq(process).and(j.dueDateExpression.like("%" + expression + "%"))).fetch();
    }

    public void deleteByToken(Token token) {
        QTimerJob tj = QTimerJob.timerJob;
        queryFactory.delete(tj).where(tj.token.eq(token)).execute();
    }

    public void deleteByProcess(Process process) {
        log.debug("deleting jobs for process " + process.getId());
        QJob j = QJob.job;
        queryFactory.delete(j).where(j.process.eq(process)).execute();
    }
}
