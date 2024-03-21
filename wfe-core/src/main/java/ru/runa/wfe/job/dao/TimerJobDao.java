package ru.runa.wfe.job.dao;

import com.querydsl.jpa.JPAExpressions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.job.DueDateInProcessTimerJob;
import ru.runa.wfe.job.InProcessTimerJob;
import ru.runa.wfe.job.QDueDateInProcessTimerJob;
import ru.runa.wfe.job.QInProcessTimerJob;
import ru.runa.wfe.job.QStartEventSubprocessTimerJob;
import ru.runa.wfe.job.QStartProcessTimerJob;
import ru.runa.wfe.job.StartEventSubprocessTimerJob;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.job.TimerJob;

/**
 * DAO for {@link TimerJob} hierarchy.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class TimerJobDao extends GenericDao<TimerJob> {

    public TimerJobDao() {
        super(TimerJob.class);
    }

    public List<TimerJob> getExpiredJobs(Long limit) {
        val now = new Date();
        val j1 = QDueDateInProcessTimerJob.dueDateInProcessTimerJob;
        val r1 = queryFactory.selectFrom(j1)
                .where(j1.dueDate.loe(now).and(j1.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(j1.token.endDate.isNull()))
                .orderBy(j1.dueDate.asc())
                .limit(limit)
                .fetch();
        limit -= r1.size();
        val j2 = QStartProcessTimerJob.startProcessTimerJob;
        List<StartProcessTimerJob> r2;
        if (limit > 0) {
            r2 = queryFactory.selectFrom(j2).where(j2.timerEventNextDate.loe(now))
                    .orderBy(j2.timerEventNextDate.asc())
                    .limit(limit)
                    .fetch();
        } else {
            r2 = new ArrayList<>();
        }
        limit -= r2.size();
        val j3 = QStartEventSubprocessTimerJob.startEventSubprocessTimerJob;
        List<StartEventSubprocessTimerJob> r3;
        if (limit > 0) {
            r3 = queryFactory.selectFrom(j3)
                    .where(j3.timerEventNextDate.loe(now))
                    .orderBy(j3.timerEventNextDate.asc())
                    .limit(limit)
                    .fetch();
        } else {
            r3 = new ArrayList<>();
        }
        // TODO Merge two SQL queries into one (this can be done e.g. during getting rid of entity polymorphism while getting rid of Hibernate).
        val result = new ArrayList<TimerJob>(r1.size() + r2.size() + r3.size());
        result.addAll(r1);
        result.addAll(r2);
        result.addAll(r3);
        return result;
    }

    public Long getExpiredJobsCount() {
        val j1 = QDueDateInProcessTimerJob.dueDateInProcessTimerJob;
        long count1 = queryFactory.selectFrom(j1)
                .where(j1.dueDate.loe(new Date()).and(j1.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(j1.token.endDate.isNull()))
                .fetchCount();
        val j2 = QStartProcessTimerJob.startProcessTimerJob;
        long count2 = queryFactory.selectFrom(j2).where(j2.timerEventNextDate.loe(new Date()))
                .fetchCount();
        val j3 = QStartEventSubprocessTimerJob.startEventSubprocessTimerJob;
        long count3 = queryFactory.selectFrom(j3)
                .where(j3.timerEventNextDate.loe(new Date()))
                .fetchCount();
        return count1 + count2 + count3;
    }
    
    public List<InProcessTimerJob> findByProcess(CurrentProcess process) {
        val j = QInProcessTimerJob.inProcessTimerJob;
        return queryFactory.selectFrom(j).where(j.process.eq(process)).orderBy(j.createDate.asc()).fetch();
    }

    public List<DueDateInProcessTimerJob> findByProcessAndDeadlineExpressionContaining(CurrentProcess process, String expression) {
        val j = QDueDateInProcessTimerJob.dueDateInProcessTimerJob;
        return queryFactory.selectFrom(j).where(j.process.eq(process).and(j.dueDateExpression.like("%" + expression + "%"))).fetch();
    }

    public void deleteByToken(CurrentToken token) {
        val j = QDueDateInProcessTimerJob.dueDateInProcessTimerJob;
        queryFactory.delete(j).where(j.token.eq(token)).execute();
    }

    public void deleteByProcess(CurrentProcess process) {
        log.debug("deleting jobs for process " + process.getId());
        val j = QInProcessTimerJob.inProcessTimerJob;
        queryFactory.delete(j).where(j.process.eq(process)).execute();
    }

    public void deleteByPack(ProcessDefinitionPack pack) {
        val j = QStartProcessTimerJob.startProcessTimerJob;
        val d = QProcessDefinition.processDefinition;
        queryFactory.delete(j).where(j.definitionId.in(JPAExpressions.select(d.id).from(d).where(d.pack.eq(pack)))).execute();
    }

    public void deleteByDefinitionId(Long definitionId) {
        val j = QStartProcessTimerJob.startProcessTimerJob;
        queryFactory.delete(j).where(j.definitionId.eq(definitionId)).execute();
    }

}
