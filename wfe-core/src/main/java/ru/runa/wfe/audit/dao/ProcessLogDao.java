package ru.runa.wfe.audit.dao;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.QNodeEnterLog;
import ru.runa.wfe.audit.QProcessLog;
import ru.runa.wfe.audit.QTransitionLog;
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

    public List<ProcessLog> getAll(@NonNull Long processId) {
        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    public List<ProcessLog> get(Process process, ProcessDefinition definition) {
        long processId = process.getId();

        QTransitionLog tl = QTransitionLog.transitionLog;
        boolean haveOldLogs = queryFactory.select(tl.id).from(tl).where(tl.processId.eq(processId).and(tl.nodeId.isNull())).fetchFirst() != null;

        if (haveOldLogs) {
            // TODO Pre 01.02.2014, remove when obsolete.
            log.debug("fallbackToOldAlgorithm in " + processId);
            List<ProcessLog> logs = getAll(processId);
            if (definition instanceof SubprocessDefinition) {
                SubprocessDefinition subprocessDefinition = (SubprocessDefinition) definition;
                String subprocessNodeId = subprocessDefinition.getParentProcessDefinition().getEmbeddedSubprocessNodeIdNotNull(
                        subprocessDefinition.getName());
                boolean embeddedSubprocessLogs = false;
                boolean childSubprocessLogs = false;
                List<String> childSubprocessNodeIds = subprocessDefinition.getEmbeddedSubprocessNodeIds();
                for (ProcessLog log : Lists.newArrayList(logs)) {
                    if (log instanceof NodeLeaveLog && Objects.equal(subprocessNodeId, log.getNodeId())) {
                        embeddedSubprocessLogs = false;
                    }
                    if (log instanceof NodeLeaveLog && childSubprocessNodeIds.contains(log.getNodeId())) {
                        childSubprocessLogs = false;
                    }
                    if (!embeddedSubprocessLogs || childSubprocessLogs) {
                        logs.remove(log);
                    }
                    if (log instanceof NodeEnterLog && childSubprocessNodeIds.contains(log.getNodeId())) {
                        childSubprocessLogs = true;
                    }
                    if (log instanceof NodeEnterLog && Objects.equal(subprocessNodeId, log.getNodeId())) {
                        embeddedSubprocessLogs = true;
                    }
                }
            } else {
                List<String> embeddedSubprocessNodeIds = definition.getEmbeddedSubprocessNodeIds();
                if (embeddedSubprocessNodeIds.size() > 0) {
                    boolean embeddedSubprocessLogs = false;
                    for (ProcessLog log : Lists.newArrayList(logs)) {
                        if (log instanceof NodeLeaveLog && embeddedSubprocessNodeIds.contains(log.getNodeId())) {
                            embeddedSubprocessLogs = false;
                        }
                        if (embeddedSubprocessLogs) {
                            logs.remove(log);
                        }
                        if (log instanceof NodeEnterLog && embeddedSubprocessNodeIds.contains(log.getNodeId())) {
                            embeddedSubprocessLogs = true;
                        }
                    }
                }
            }
            return logs;
        }

        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl)
                .where(pl.processId.eq(processId))
                .where(definition instanceof SubprocessDefinition ? pl.nodeId.like(definition.getNodeId() + ".%") : pl.nodeId.notLike("sub%"))
                .orderBy(pl.id.asc())
                .fetch();
    }

    /**
     * Deletes all process logs.
     */
    public void deleteAll(Long processId) {
        log.debug("deleting logs for process " + processId);
        QProcessLog pl = QProcessLog.processLog;
        queryFactory.delete(pl).where(pl.processId.eq(processId)).execute();
    }

    public boolean isNodeEntered(Process process, String nodeId) {
        QNodeEnterLog nel = QNodeEnterLog.nodeEnterLog;
        return queryFactory.select(nel.id).from(nel).where(nel.processId.eq(process.getId()).and(nel.nodeId.eq(nodeId))).fetchFirst() != null;
    }
}
