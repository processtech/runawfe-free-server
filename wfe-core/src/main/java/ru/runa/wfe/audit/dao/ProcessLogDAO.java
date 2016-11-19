package ru.runa.wfe.audit.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

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
        return getHibernateTemplate().find("from ProcessLog where processId=? order by id asc", processId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLog> get(Long processId, ProcessDefinition definition) {
        String checkQuery = "select count(t) from TransitionLog t where processId=? and t.nodeId is null";
        Number oldLogsCount = (Number) getHibernateTemplate().find(checkQuery, processId).get(0);
        boolean fallbackToOldAlgorithm = oldLogsCount.intValue() > 0;
        if (fallbackToOldAlgorithm) {
            log.debug("fallbackToOldAlgorithm in " + processId);
            List<ProcessLog> logs = getAll(processId);
            // pre 01.02.2014, remove as it will be obsolete
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
        if (definition instanceof SubprocessDefinition) {
            return getHibernateTemplate().find("from ProcessLog where processId=? and nodeId like ? order by id asc", processId,
                    definition.getNodeId() + ".%");
        } else {
            return getHibernateTemplate().find("from ProcessLog where processId=? and nodeId not like 'sub%' order by id asc", processId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLog> getAll(final ProcessLogFilter filter) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<ProcessLog>>() {

            @Override
            public List<ProcessLog> doInHibernate(Session session) {
                boolean filterBySeverity = filter.getSeverities().size() != 0 && filter.getSeverities().size() != Severity.values().length;
                String hql = "from ProcessLog where processId = :processId";
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
                Query query = session.createQuery(hql);
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
                return query.list();
            }
        });
    }

    /**
     * Deletes all process logs.
     */
    public void deleteAll(Long processId) {
        log.debug("deleting logs for process " + processId);
        getHibernateTemplate().bulkUpdate("delete from ProcessLog where processId=?", processId);
    }

    public boolean isNodeEntered(Process process, String nodeId) {
        return getHibernateTemplate().find("from NodeEnterLog where processId=? and nodeId=?", process.getId(), nodeId).size() > 0;
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
