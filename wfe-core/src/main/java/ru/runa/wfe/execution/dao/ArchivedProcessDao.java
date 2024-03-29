package ru.runa.wfe.execution.dao;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.execution.ArchivedProcess;
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

    public List<ArchivedProcess> findAllByDefinitionPackOrderByStartDateAsc(ProcessDefinitionPack pack) {
        val p = QArchivedProcess.archivedProcess;
        return queryFactory.selectFrom(p).where(p.definition.pack.eq(pack)).orderBy(p.startDate.desc()).fetch();
    }

    public List<ArchivedProcess> findAllByDefinitionOrderByStartDateAsc(ProcessDefinition definition) {
        val p = QArchivedProcess.archivedProcess;
        return queryFactory.selectFrom(p).where(p.definition.eq(definition)).orderBy(p.startDate.desc()).fetch();
    }

    public void delete(ArchivedProcess process) {
        Preconditions.checkNotNull(process);
        sessionFactory.getCurrentSession().delete(process);
    }

    public void flushPendingChanges() {
        sessionFactory.getCurrentSession().flush();
    }

}
