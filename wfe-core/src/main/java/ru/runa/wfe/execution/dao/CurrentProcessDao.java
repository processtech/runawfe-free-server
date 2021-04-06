package ru.runa.wfe.execution.dao;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.definition.QProcessDefinitionVersion;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.QCurrentNodeProcess;
import ru.runa.wfe.execution.QCurrentProcess;
import ru.runa.wfe.execution.QCurrentSwimlane;
import ru.runa.wfe.execution.QCurrentToken;
import ru.runa.wfe.task.QTask;
import ru.runa.wfe.user.Executor;

@Component
public class CurrentProcessDao extends GenericDao<CurrentProcess> {

    public CurrentProcessDao() {
        super(CurrentProcess.class);
    }

    @Override
    protected void checkNotNull(CurrentProcess entity, Object identity) {
        if (entity == null) {
            throw new ProcessDoesNotExistException(identity);
        }
    }

    /**
     * Checks that no parent processes exists with different definition. If some exists, return its definition name.
     * <p>
     * This is optimized query to check if given process definition can be deleted.
     *
     * @return Null if not found.
     */
    public String findParentProcessDefinitionName(long processDefinitionId) {
        QProcessDefinition d0 = new QProcessDefinition("d0");
        QProcessDefinitionVersion dv = new QProcessDefinitionVersion("dv");
        QProcessDefinitionVersion dv0 = new QProcessDefinitionVersion("dv0");
        QCurrentProcess p = new QCurrentProcess("p");
        QCurrentProcess p0 = new QCurrentProcess("p0");
        QCurrentNodeProcess np = new QCurrentNodeProcess("np");
        return queryFactory.select(d0.name)
                .from(dv, p, np, p0, dv0, d0)
                .where(dv.definition.id.eq(processDefinitionId)
                        .and(p.definitionVersion.eq(dv))
                        .and(np.subProcess.eq(p))
                        .and(p0.eq(np.process))
                        .and(dv0.eq(p0.definitionVersion))
                        .and(dv0.definition.id.ne(processDefinitionId))
                        .and(d0.eq(dv0.definition))
                )
                .fetchFirst();
    }

    /**
     * Fetches all processes for ALL given process definition versions. The returned list of processs is sorted start date, youngest first.
     */
    public List<CurrentProcess> findAllProcessesForAllDefinitionVersions(Long processDefinitionId) {
        QCurrentProcess p = QCurrentProcess.currentProcess;
        return queryFactory.selectFrom(p)
                .where(p.definitionVersion.definition.id.eq(processDefinitionId))
                .orderBy(p.startDate.desc())
                .fetch();
    }

    List<CurrentProcess> findImpl(List<Long> ids) {
        val p = QCurrentProcess.currentProcess;
        return queryFactory.selectFrom(p).where(p.id.in(ids)).fetch();
    }

    public Set<Long> getDependentProcessIds(Executor executor, int limit) {
        Preconditions.checkArgument(limit > 0);
        val s = QCurrentSwimlane.currentSwimlane;
        val result = new HashSet<Long>(queryFactory
                .selectDistinct(s.process.id)
                .from(s)
                .where(s.executor.eq(executor))
                .limit(limit)
                .fetch());
        if (result.size() < limit) {
            val t = QTask.task;
            result.addAll(queryFactory
                    .selectDistinct(t.process.id)
                    .from(t)
                    .where(t.executor.eq(executor))
                    .limit(limit - result.size())
                    .fetch());
        }
        return result;
    }

    public List<CurrentProcess> getProcesses(final ProcessFilter filter) {
        val p = QCurrentProcess.currentProcess;
        val q = queryFactory.selectFrom(p).where();
        if (filter.getDefinitionName() != null) {
            q.where(p.definitionVersion.definition.name.eq(filter.getDefinitionName()));
        }
        if (filter.getDefinitionVersion() != null) {
            q.where(p.definitionVersion.version.eq(filter.getDefinitionVersion()));
        }
        if (filter.getId() != null) {
            q.where(p.id.eq(filter.getId()));
        }
        if (filter.getIdFrom() != null) {
            q.where(p.id.goe(filter.getIdFrom()));
        }
        if (filter.getIdTo() != null) {
            q.where(p.id.loe(filter.getIdTo()));
        }
        if (filter.getStartDateFrom() != null) {
            q.where(p.startDate.goe(filter.getStartDateFrom()));
        }
        if (filter.getStartDateTo() != null) {
            q.where(p.startDate.loe(filter.getStartDateTo()));
        }
        if (filter.getFinished() != null) {
            q.where(filter.getFinished() ? p.endDate.isNotNull() : p.endDate.isNull());
        }
        if (filter.getEndDateFrom() != null) {
            q.where(p.endDate.goe(filter.getEndDateFrom()));
        }
        if (filter.getEndDateTo() != null) {
            q.where(p.endDate.loe(filter.getEndDateTo()));
        }
        if (filter.getFailedOnly()) {
            q.where(p.executionStatus.eq(ExecutionStatus.FAILED));
        }
        return q.fetch();
    }

    @Override
    public void delete(CurrentProcess process) {
        log.debug("deleting tokens for " + process);
        val t = QCurrentToken.currentToken;
        // TODO Try delete.where (order matters due to foreign keys)
        List<CurrentToken> tokens = queryFactory.selectFrom(t).where(t.process.eq(process).and(t.parent.isNotNull())).orderBy(t.id.desc()).fetch();
        for (CurrentToken token : tokens) {
            log.debug("deleting " + token);
            sessionFactory.getCurrentSession().delete(token);
        }
        super.delete(process);
    }
}
