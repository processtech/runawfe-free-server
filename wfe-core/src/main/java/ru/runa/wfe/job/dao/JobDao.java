package ru.runa.wfe.job.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import com.querydsl.core.types.dsl.PathBuilder;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.QJob;
import ru.runa.wfe.job.QStartProcessTimerJob;
import ru.runa.wfe.job.QTimerJob;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.job.TimerJob;

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

    public List<TimerJob> getExpiredJobs(Long limit) {
        val now = new Date();
        PathBuilder<Job> timerJobPath = new PathBuilder<>(Job.class, "timerJob");
        val j1 = QTimerJob.timerJob;
        val r1 = queryFactory.select(j1).from(timerJobPath)
                .where(j1.dueDate.loe(now).and(j1.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(j1.token.endDate.isNull()))
                .orderBy(j1.dueDate.asc())
                .limit(limit)
                .fetch();
        limit -= r1.size();
        PathBuilder<Job> startProcessTimerJobPath = new PathBuilder<>(Job.class, "startProcessTimerJob");
        val j2 = QStartProcessTimerJob.startProcessTimerJob;
        List<StartProcessTimerJob> r2;
        if (limit > 0) {
            r2 = queryFactory.select(j2).from(startProcessTimerJobPath)
                    .where(j2.timerEventNextDate.isNotNull().and(j2.timerEventNextDate.loe(now)))
                    .orderBy(j2.timerEventNextDate.asc())
                    .limit(limit)
                    .fetch();
        } else {
            r2 = new ArrayList<>();
        }

        val result = new ArrayList<TimerJob>(r1.size() + r2.size());
        result.addAll(r1);
        result.addAll(r2);
        return result;
    }

    public Long getExpiredJobsCount() {
        val j1 = QJob.job;
        long count1 = queryFactory.selectFrom(j1)
                .where(j1.dueDate.loe(new Date()).and(j1.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(j1.token.endDate.isNull()))
                .fetchCount();
        val j2 = QStartProcessTimerJob.startProcessTimerJob;
        PathBuilder<Job> startProcessTimerJobPath = new PathBuilder<>(Job.class, "startProcessTimerJob");
        long count2 = queryFactory.select(j2).from(startProcessTimerJobPath)
                .where(j2.timerEventNextDate.isNotNull().and(j2.timerEventNextDate.loe(new Date())))
                .fetchCount();
        return count1 + count2;
    }

    public List<TimerJob> findByProcess(CurrentProcess process) {
        val j = QTimerJob.timerJob;
        return queryFactory.selectFrom(j).where(j.process.eq(process)).orderBy(j.dueDate.asc()).fetch();
    }

    public List<TimerJob> findByProcessAndDeadlineExpressionContaining(CurrentProcess process, String expression) {
        val j = QTimerJob.timerJob;
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
