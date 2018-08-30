package ru.runa.wfe.audit.dao;

import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentProcessLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.QCurrentNodeEnterLog;
import ru.runa.wfe.audit.QCurrentProcessLog;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

/**
 * DAO for {@link CurrentProcessLog}.
 * 
 * @author dofs
 */
@Component
public class CurrentProcessLogDao extends GenericDao<CurrentProcessLog> implements BaseProcessLogDao {

    public CurrentProcessLogDao() {
        super(CurrentProcessLog.class);
    }

    @Override
    public Class<? extends BaseProcessLog> typeToClass(ProcessLog.Type type) {
        return type.currentRootClass;
    }

    List<CurrentProcessLog> getAll(@NonNull Long processId) {
        val pl = QCurrentProcessLog.currentProcessLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    List<CurrentProcessLog> get(CurrentProcess process, ProcessDefinition definition) {
        val pl = QCurrentProcessLog.currentProcessLog;
        return queryFactory.selectFrom(pl)
                .where(pl.processId.eq(process.getId()))
                .where(definition instanceof SubprocessDefinition ? pl.nodeId.like(definition.getNodeId() + ".%") : pl.nodeId.notLike("sub%"))
                .orderBy(pl.id.asc())
                .fetch();
    }

    /**
     * Deletes all process logs.
     */
    void deleteAll(CurrentProcess process) {
        long processId = process.getId();
        log.debug("deleting logs for process " + processId);
        val pl = QCurrentProcessLog.currentProcessLog;
        queryFactory.delete(pl).where(pl.processId.eq(processId)).execute();
    }

    boolean isNodeEntered(CurrentProcess process, String nodeId) {
        val nel = QCurrentNodeEnterLog.currentNodeEnterLog;
        return queryFactory.select(nel.id).from(nel).where(nel.processId.eq(process.getId()).and(nel.nodeId.eq(nodeId))).fetchFirst() != null;
    }
}
