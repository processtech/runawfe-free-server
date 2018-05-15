package ru.runa.wfe.audit.dao;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.QNodeEnterLog;
import ru.runa.wfe.audit.QProcessLog;
import ru.runa.wfe.audit.QTransitionLog;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

/**
 * DAO for {@link ProcessLog}.
 * 
 * @author dofs
 * @since 4.0
 */
public class ProcessLogDAO extends GenericDAO<ProcessLog> implements IProcessLogDAO<ProcessLog> {

    @Autowired
    private ProcessLogAwareDao customizationDao;

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLog> getAll(Long processId) {
        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLog> get(Long processId, ProcessDefinition definition) {
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

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLog> getAll(final ProcessLogFilter filter) {
        Preconditions.checkArgument(ProcessLog.class.isAssignableFrom(ClassLoaderUtil.loadClass(filter.getRootClassName())),
                "invalid filter root class name");

        boolean filterBySeverity = filter.getSeverities().size() != 0 && filter.getSeverities().size() != Severity.values().length;
        String hql = "from " + filter.getRootClassName() + " where processId = :processId";
        if (filter.getIdFrom() != null) {
            hql += " and id >= :idFrom";
        }
        if (filter.getIdTo() != null) {
            hql += " and id <= :idTo";
        }
        if (filter.getCreateDateFrom() != null) {
            hql += " and createDate >= :createDateFrom";
        }
        if (filter.getCreateDateTo() != null) {
            hql += " and createDate <= :createDateTo";
        }
        if (filter.getTokenId() != null) {
            hql += " and tokenId = :tokenId";
        }
        if (filter.getNodeId() != null) {
            hql += " and nodeId = :nodeId";
        }
        if (filterBySeverity) {
            hql += " and severity in (:severities)";
        }
        hql += " order by id asc";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("processId", filter.getProcessId());
        if (filter.getIdFrom() != null) {
            query.setParameter("idFrom", filter.getIdFrom());
        }
        if (filter.getIdTo() != null) {
            query.setParameter("idTo", filter.getIdTo());
        }
        if (filter.getCreateDateFrom() != null) {
            query.setParameter("createDateFrom", filter.getCreateDateFrom());
        }
        if (filter.getCreateDateTo() != null) {
            query.setParameter("createDateTo", filter.getCreateDateTo());
        }
        if (filter.getTokenId() != null) {
            query.setParameter("tokenId", filter.getTokenId());
        }
        if (filter.getNodeId() != null) {
            query.setParameter("nodeId", filter.getNodeId());
        }
        if (filterBySeverity) {
            query.setParameterList("severities", filter.getSeverities());
        }
        return (List<ProcessLog>) query.list();
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

    public void addLog(ProcessLog processLog, Process process, Token token) {
        processLog.setProcessId(process.getId());
        if (token == null) {
            token = process.getRootToken();
        }
        processLog.setTokenId(token.getId());
        if (processLog.getNodeId() == null) {
            processLog.setNodeId(token.getNodeId());
        }
        processLog.setCreateDate(new Date());
        this.create(processLog);
        registerInCustomizationDao(processLog, process, token);
    }

    private void registerInCustomizationDao(ProcessLog processLog, Process process, Token token) {
        try {
            customizationDao.addLog(processLog, process, token);
        } catch (Throwable e) {
            log.warn("Custom log handler throws exception", e);
        }
    }

}
