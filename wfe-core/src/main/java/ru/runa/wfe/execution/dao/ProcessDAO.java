package ru.runa.wfe.execution.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class ProcessDAO extends GenericDAO<Process> {

    @Override
    protected void checkNotNull(Process entity, Object identity) {
        if (entity == null) {
            throw new ProcessDoesNotExistException(identity);
        }
    }

    /**
     * fetches all processes for the given process definition from the database.
     * The returned list of processs is sorted start date, youngest first.
     */
    public List<Process> findAllProcesses(Long definitionId) {
        return getHibernateTemplate().find("from Process where deployment.id=? order by startDate desc", definitionId);
    }

    public Set<Number> getDependentProcessIds(Executor executor) {
        Set<Number> processes = Sets.newHashSet();
        processes.addAll(getHibernateTemplate().find("select process.id from Swimlane where executor=?", executor));
        processes.addAll(getHibernateTemplate().find("select process.id from Task where executor=?", executor));
        return processes;
    }

    public List<Process> getProcesses(final ProcessFilter filter) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Process>>() {

            @Override
            public List<Process> doInHibernate(Session session) {
                List<String> conditions = Lists.newArrayList();
                Map<String, Object> parameters = Maps.newHashMap();
                if (filter.getDefinitionName() != null) {
                    conditions.add("deployment.name = :definitionName");
                    parameters.put("definitionName", filter.getDefinitionName());
                }
                if (filter.getDefinitionVersion() != null) {
                    conditions.add("deployment.version = :definitionVersion");
                    parameters.put("definitionVersion", filter.getDefinitionVersion());
                }
                if (filter.getId() != null) {
                    conditions.add("id = :id");
                    parameters.put("id", filter.getId());
                }
                if (filter.getIdFrom() != null) {
                    conditions.add("id >= :idFrom");
                    parameters.put("idFrom", filter.getIdFrom());
                }
                if (filter.getIdTo() != null) {
                    conditions.add("id <= :idTo");
                    parameters.put("idTo", filter.getIdTo());
                }
                if (filter.getStartDateFrom() != null) {
                    conditions.add("startDate >= :startDateFrom");
                    parameters.put("startDateFrom", filter.getStartDateFrom());
                }
                if (filter.getStartDateTo() != null) {
                    conditions.add("startDate <= :startDateTo");
                    parameters.put("startDateTo", filter.getStartDateTo());
                }
                if (filter.getFinished() != null) {
                    if (filter.getFinished()) {
                        conditions.add("endDate is not null");
                    } else {
                        conditions.add("endDate is null");
                    }
                }
                if (filter.getEndDateFrom() != null) {
                    conditions.add("endDate >= :endDateFrom");
                    parameters.put("endDateFrom", filter.getEndDateFrom());
                }
                if (filter.getEndDateTo() != null) {
                    conditions.add("endDate <= :endDateTo");
                    parameters.put("endDateTo", filter.getEndDateTo());
                }
                if (conditions.size() == 0) {
                    throw new IllegalArgumentException("Filter should be specified");
                }
                String hql = "from Process where " + Joiner.on(" and ").join(conditions);
                Query query = session.createQuery(hql);
                for (Entry<String, Object> param : parameters.entrySet()) {
                    query.setParameter(param.getKey(), param.getValue());
                }
                return query.list();
            }
        });
    }

    @Override
    public void delete(Process process) {
        log.debug("deleting tokens for " + process);
        List<Token> tokens = getHibernateTemplate().find("from Token where process=? and parent is not null order by id desc", process);
        for (Token token : tokens) {
            log.debug("deleting " + token);
            getHibernateTemplate().delete(token);
        }
        super.delete(process);
    }
}
