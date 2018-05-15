package ru.runa.wfe.execution.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.QProcess;
import ru.runa.wfe.execution.QSwimlane;
import ru.runa.wfe.execution.QToken;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.QTask;
import ru.runa.wfe.user.Executor;

public class ProcessDAO extends GenericDAO<Process> {

    @Override
    protected void checkNotNull(Process entity, Object identity) {
        if (entity == null) {
            throw new ProcessDoesNotExistException(identity);
        }
    }

    /**
     * fetches all processes for the given process definition from the database. The returned list of processs is sorted start date, youngest first.
     */
    public List<Process> findAllProcesses(Long definitionId) {
        QProcess p = QProcess.process;
        return queryFactory.selectFrom(p).where(p.deployment.id.eq(definitionId)).orderBy(p.startDate.desc()).fetch();
    }

    public List<Process> find(List<Long> ids) {
        if (ids.isEmpty()) {
            return Lists.newArrayList();
        }
        QProcess p = QProcess.process;
        return queryFactory.selectFrom(p).where(p.id.in(ids)).fetch();
    }

    public Set<Number> getDependentProcessIds(Executor executor) {
        Set<Number> processes = Sets.newHashSet();
        QSwimlane s = QSwimlane.swimlane;
        processes.addAll(queryFactory.select(s.process.id).from(s).where(s.executor.eq(executor)).fetch());
        QTask t = QTask.task;
        processes.addAll(queryFactory.select(t.process.id).from(t).where(t.executor.eq(executor)).fetch());
        return processes;
    }

    public List<Process> getProcesses(final ProcessFilter filter) {
        QProcess p = QProcess.process;
        JPQLQuery<Process> q = queryFactory.selectFrom(p).where();

        boolean emptyFilter = true;
        if (filter.getDefinitionName() != null) {
            q.where(p.deployment.name.eq(filter.getDefinitionName()));
            emptyFilter = false;
        }
        if (filter.getDefinitionVersion() != null) {
            q.where(p.deployment.version.eq(filter.getDefinitionVersion()));
            emptyFilter = false;
        }
        if (filter.getId() != null) {
            q.where(p.id.eq(filter.getId()));
            emptyFilter = false;
        }
        if (filter.getIdFrom() != null) {
            q.where(p.id.goe(filter.getIdFrom()));
            emptyFilter = false;
        }
        if (filter.getIdTo() != null) {
            q.where(p.id.loe(filter.getIdTo()));
            emptyFilter = false;
        }
        if (filter.getStartDateFrom() != null) {
            q.where(p.startDate.goe(filter.getStartDateFrom()));
            emptyFilter = false;
        }
        if (filter.getStartDateTo() != null) {
            q.where(p.startDate.loe(filter.getStartDateTo()));
            emptyFilter = false;
        }
        if (filter.getFinished() != null) {
            q.where(filter.getFinished() ? p.endDate.isNotNull() : p.endDate.isNull());
            emptyFilter = false;
        }
        if (filter.getEndDateFrom() != null) {
            q.where(p.endDate.goe(filter.getEndDateFrom()));
            emptyFilter = false;
        }
        if (filter.getEndDateTo() != null) {
            q.where(p.endDate.loe(filter.getEndDateTo()));
            emptyFilter = false;
        }
        if (filter.getFailedOnly()) {
            q.where(p.executionStatus.eq(ExecutionStatus.FAILED));
            emptyFilter = false;
        }
        if (emptyFilter) {
            throw new IllegalArgumentException("Filter should be specified");
        }

        return q.fetch();
    }

    @Override
    public void delete(Process process) {
        log.debug("deleting tokens for " + process);
        QToken t = QToken.token;
        List<Token> tokens = queryFactory.selectFrom(t).where(t.process.eq(process).and(t.parent.isNotNull())).orderBy(t.id.desc()).fetch();
        for (Token token : tokens) {
            log.debug("deleting " + token);
            sessionFactory.getCurrentSession().delete(token);
        }
        super.delete(process);
    }
}
