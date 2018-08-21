package ru.runa.wfe.audit.dao;

import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ArchivedProcessLog;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.QArchivedProcessLog;
import ru.runa.wfe.audit.QNodeEnterLog;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

@Component
public class ArchivedProcessLogDao extends BaseProcessLogDao<ArchivedProcessLog> {

    @Override
    protected Class<? extends BaseProcessLog> typeToClass(IProcessLog.Type type) {
        return type.archivedRootClass;
    }

    public List<ArchivedProcessLog> getAll(@NonNull Long processId) {
        QArchivedProcessLog pl = QArchivedProcessLog.archivedProcessLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    // TODO Do we need to copy-paste old processing from ProcessLogDao.get(process, definition) too?
    public List<ArchivedProcessLog> get(ArchivedProcess process, ProcessDefinition definition) {
        QArchivedProcessLog pl = QArchivedProcessLog.archivedProcessLog;
        return queryFactory.selectFrom(pl)
                .where(pl.processId.eq(process.getId()))
                .where(definition instanceof SubprocessDefinition ? pl.nodeId.like(definition.getNodeId() + ".%") : pl.nodeId.notLike("sub%"))
                .orderBy(pl.id.asc())
                .fetch();
    }
}
