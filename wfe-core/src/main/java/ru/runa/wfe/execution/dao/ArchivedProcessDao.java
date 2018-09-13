package ru.runa.wfe.execution.dao;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.QArchivedProcess;
import ru.runa.wfe.execution.QArchivedSwimlane;
import ru.runa.wfe.user.Executor;

@Component
public class ArchivedProcessDao extends ReadOnlyGenericDao<ArchivedProcess> {

    public ArchivedProcessDao() {
        super(ArchivedProcess.class);
    }

    List<ArchivedProcess> findImpl(List<Long> ids) {
        val p = QArchivedProcess.archivedProcess;
        return queryFactory.selectFrom(p).where(p.id.in(ids)).fetch();
    }

    public boolean processesExist(Deployment d) {
        val p = QArchivedProcess.archivedProcess;
        return queryFactory.select(p.id).from(p).where(p.deployment.id.eq(d.getId())).limit(1).fetchFirst() != null;
    }

    Set<Long> getDependentProcessIds(Executor executor, int limit) {
        Preconditions.checkArgument(limit > 0);
        val s = QArchivedSwimlane.archivedSwimlane;
        return new HashSet<>(queryFactory
                .selectDistinct(s.process.id)
                .from(s)
                .where(s.executor.eq(executor))
                .limit(limit)
                .fetch());
    }

    // TODO Unused if ProcessDao.getProcesses(filter) is unused.
    public List<ArchivedProcess> getProcesses(final ProcessFilter filter) {
        if (filter.getFailedOnly()) {
            return Collections.emptyList();
        }

        val p = QArchivedProcess.archivedProcess;
        val q = queryFactory.selectFrom(p).where();
        if (filter.getDefinitionName() != null) {
            q.where(p.deployment.name.eq(filter.getDefinitionName()));
        }
        if (filter.getDefinitionVersion() != null) {
            q.where(p.deployment.version.eq(filter.getDefinitionVersion()));
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
        return q.fetch();
    }
}
