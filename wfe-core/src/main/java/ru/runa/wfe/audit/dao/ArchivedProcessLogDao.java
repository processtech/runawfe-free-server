package ru.runa.wfe.audit.dao;

import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ArchivedProcessLog;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.QArchivedNodeEnterLog;
import ru.runa.wfe.audit.QArchivedProcessLog;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;

@Component
public class ArchivedProcessLogDao extends ReadOnlyGenericDao<ArchivedProcessLog> {

    public ArchivedProcessLogDao() {
        super(ArchivedProcessLog.class);
    }

    public List<BaseProcessLog> getAll(final ProcessLogFilter filter) {
        return CommonProcessLogDao.getAll(filter, filter.getType().archivedRootClass);
    }

    List<ArchivedProcessLog> getAll(@NonNull Long processId) {
        val pl = QArchivedProcessLog.archivedProcessLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    List<ArchivedProcessLog> get(ArchivedProcess process, ParsedProcessDefinition definition) {
        val pl = QArchivedProcessLog.archivedProcessLog;
        return queryFactory.selectFrom(pl)
                .where(pl.processId.eq(process.getId()))
                .where(definition instanceof ParsedSubprocessDefinition ? pl.nodeId.like(definition.getNodeId() + ".%") : pl.nodeId.notLike("sub%"))
                .orderBy(pl.id.asc())
                .fetch();
    }

    boolean isNodeEntered(ArchivedProcess process, String nodeId) {
        val nel = QArchivedNodeEnterLog.archivedNodeEnterLog;
        return queryFactory.select(nel.id).from(nel).where(nel.processId.eq(process.getId()).and(nel.nodeId.eq(nodeId))).fetchFirst() != null;
    }

    public void deleteAll(ArchivedProcess process) {
        long processId = process.getId();
        log.debug("deleting logs for archived process " + processId);
        val pl = QArchivedProcessLog.archivedProcessLog;
        queryFactory.delete(pl).where(pl.processId.eq(processId)).execute();
    }
}
