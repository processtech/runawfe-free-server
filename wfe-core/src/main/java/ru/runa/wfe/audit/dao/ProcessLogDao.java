package ru.runa.wfe.audit.dao;

import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.QNodeEnterLog;
import ru.runa.wfe.audit.QProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

/**
 * DAO for {@link ProcessLog}.
 * 
 * @author dofs
 */
@Component
public class ProcessLogDao extends BaseProcessLogDao<ProcessLog> {

    @Override
    protected Class<? extends BaseProcessLog> typeToClass(IProcessLog.Type type) {
        return type.currentRootClass;
    }

    List<ProcessLog> getAll(@NonNull Long processId) {
        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    List<ProcessLog> get(Process process, ProcessDefinition definition) {
        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl)
                .where(pl.processId.eq(process.getId()))
                .where(definition instanceof SubprocessDefinition ? pl.nodeId.like(definition.getNodeId() + ".%") : pl.nodeId.notLike("sub%"))
                .orderBy(pl.id.asc())
                .fetch();
    }

    /**
     * Deletes all process logs.
     */
    void deleteAll(Process process) {
        long processId = process.getId();
        log.debug("deleting logs for process " + processId);
        QProcessLog pl = QProcessLog.processLog;
        queryFactory.delete(pl).where(pl.processId.eq(processId)).execute();
    }

    boolean isNodeEntered(Process process, String nodeId) {
        QNodeEnterLog nel = QNodeEnterLog.nodeEnterLog;
        return queryFactory.select(nel.id).from(nel).where(nel.processId.eq(process.getId()).and(nel.nodeId.eq(nodeId))).fetchFirst() != null;
    }
}
