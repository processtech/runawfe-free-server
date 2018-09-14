package ru.runa.wfe.execution.dao;

import com.querydsl.jpa.JPQLQuery;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.definition.QProcessDefinitionVersion;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.QNodeProcess;
import ru.runa.wfe.execution.QProcess;
import ru.runa.wfe.execution.QSwimlane;
import ru.runa.wfe.execution.QToken;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.QTask;
import ru.runa.wfe.user.Executor;

@Component
public class ProcessDao extends GenericDao<Process> {

    @Override
    protected void checkNotNull(Process entity, Object identity) {
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
    public String findParentProcessDefinitionName(Long processDefinitionId) {
        QProcessDefinition d0 = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        QProcessDefinitionVersion dv0 = QProcessDefinitionVersion.processDefinitionVersion;
        QProcess p = QProcess.process;
        QProcess p0 = QProcess.process;
        QNodeProcess np = QNodeProcess.nodeProcess;
        return queryFactory.select(d0.name)
                .from(dv, p, np, p0, dv0, d0)
                .where(dv.processDefinition.id.eq(processDefinitionId)
                        .and(p.processDefinitionVersion.eq(dv))
                        .and(np.subProcess.eq(p))
                        .and(p0.eq(np.process))
                        .and(dv0.eq(p0.processDefinitionVersion))
                        .and(dv0.processDefinition.id.ne(processDefinitionId))
                        .and(d0.eq(dv0.processDefinition))
                )
                .fetchFirst();
    }

    /**
     * Fetches all processes for ALL given process definition versions. The returned list of processs is sorted start date, youngest first.
     */
    public List<Process> findAllProcessesForAllDefinitionVersions(Long processDefinitionId) {
        QProcess p = QProcess.process;
        return queryFactory.selectFrom(p)
                .where(p.processDefinitionVersion.processDefinition.id.eq(processDefinitionId))
                .orderBy(p.startDate.desc())
                .fetch();
    }

    public List<Process> find(List<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        QProcess p = QProcess.process;
        return queryFactory.selectFrom(p).where(p.id.in(ids)).fetch();
    }

    public Set<Long> getDependentProcessIds(Executor executor) {
        Set<Long> processes = new HashSet<>();
        QSwimlane s = QSwimlane.swimlane;
        processes.addAll(queryFactory.selectDistinct(s.process.id).from(s).where(s.executor.eq(executor)).fetch());
        QTask t = QTask.task;
        processes.addAll(queryFactory.selectDistinct(t.process.id).from(t).where(t.executor.eq(executor)).fetch());
        return processes;
    }

    public List<Process> getProcesses(final ProcessFilter filter) {
        QProcess p = QProcess.process;
        JPQLQuery<Process> q = queryFactory.selectFrom(p).where();
        if (filter.getDefinitionName() != null) {
            q.where(p.processDefinitionVersion.processDefinition.name.eq(filter.getDefinitionName()));
        }
        if (filter.getDefinitionVersion() != null) {
            q.where(p.processDefinitionVersion.version.eq(filter.getDefinitionVersion()));
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
    public void delete(Process process) {
        log.debug("deleting tokens for " + process);
        QToken t = QToken.token;
        // TODO Try delete.where (order matters due to foreign keys)
        List<Token> tokens = queryFactory.selectFrom(t).where(t.process.eq(process).and(t.parent.isNotNull())).orderBy(t.id.desc()).fetch();
        for (Token token : tokens) {
            log.debug("deleting " + token);
            sessionFactory.getCurrentSession().delete(token);
        }
        super.delete(process);
    }
}
