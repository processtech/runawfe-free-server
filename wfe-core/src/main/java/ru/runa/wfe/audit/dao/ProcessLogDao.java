package ru.runa.wfe.audit.dao;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogVisitor;
import ru.runa.wfe.audit.ProcessLogsCleanLog;
import ru.runa.wfe.audit.QNodeEnterLog;
import ru.runa.wfe.audit.QProcessLog;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.audit.VariableHistoryStateFilter;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;

import static ru.runa.wfe.audit.Attributes.ATTR_VARIABLE_NAME;

/**
 * DAO for {@link ProcessLog}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class ProcessLogDao extends GenericDao<ProcessLog> {

    @Autowired
    private List<ProcessLogVisitor> processLogVisitors;

    @Autowired
    protected SystemLogDao systemLogDao;

    public List<ProcessLog> getAll(Long processId) {
        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl).where(pl.processId.eq(processId)).orderBy(pl.id.asc()).fetch();
    }

    public List<ProcessLog> get(Long processId, ProcessDefinition definition) {
        QProcessLog pl = QProcessLog.processLog;
        return queryFactory.selectFrom(pl)
                .where(pl.processId.eq(processId))
                .where(definition instanceof SubprocessDefinition ? pl.nodeId.like(definition.getNodeId() + ".%") : pl.nodeId.notLike("sub%"))
                .orderBy(pl.id.asc())
                .fetch();
    }

    @SuppressWarnings("unchecked")
    public List<ProcessLog> getAll(final ProcessLogFilter filter) {
        Preconditions.checkArgument(ProcessLog.class.isAssignableFrom(ClassLoaderUtil.loadClass(filter.getRootClassName())),
                "invalid filter root class name");

        boolean filterBySeverity = filter.getSeverities().size() != 0 && filter.getSeverities().size() != Severity.values().length;
        String variableName = filter instanceof VariableHistoryStateFilter ? ((VariableHistoryStateFilter) filter).getVariableName() : null;
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
        if (!Utils.isNullOrEmpty(variableName)) {
            hql += " and (content like :contentLikeExpression or content like :contentLikeExpressionWithUserTypeDELIM))";
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
        if (!Utils.isNullOrEmpty(variableName)) {
            query.setParameter("contentLikeExpression", "%<" + ATTR_VARIABLE_NAME + ">" + variableName + "</" + ATTR_VARIABLE_NAME + ">%");
            query.setParameter("contentLikeExpressionWithUserTypeDELIM", "%<" + ATTR_VARIABLE_NAME + ">" + variableName + UserType.DELIM + "%");
        }
        return query.list();
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
        if (token != null) {
            processLog.setTokenId(token.getId());
        }
        if (processLog.getNodeId() == null && token != null) {
            processLog.setNodeId(token.getNodeId());
        }
        processLog.setCreateDate(new Date());
        processLog.serializeAttributes();
        this.create(processLog);
        for (ProcessLogVisitor processLogVisitor : processLogVisitors) {
            processLog.processBy(processLogVisitor);
        }
    }

    public void deleteBeforeDate(User user, Date date) {
        QProcessLog pl = QProcessLog.processLog;
        queryFactory.delete(pl).where(pl.createDate.before(date)).execute();
        systemLogDao.create(new ProcessLogsCleanLog(user.getActor().getId(), date));
    }
}
