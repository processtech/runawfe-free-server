package ru.runa.wfe.execution.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class NodeProcessDAO extends GenericDAO<NodeProcess> {

    public NodeProcess findBySubProcessId(Long processId) {
        return findFirstOrNull("from NodeProcess where subProcess.id = ?", processId);
    }

    public NodeProcess findByParentToken(Token parentToken) {
        return findFirstOrNull("from NodeProcess where parentToken = ?", parentToken);
    }

    public List<NodeProcess> getNodeProcesses(final Process process, final Token parentToken, final String nodeId, final Boolean finished) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Process>>() {

            @Override
            public List<Process> doInHibernate(Session session) {
                List<String> conditions = Lists.newArrayList();
                Map<String, Object> parameters = Maps.newHashMap();
                if (process != null) {
                    conditions.add("process=:process");
                    parameters.put("process", process);
                }
                if (parentToken != null) {
                    conditions.add("parentToken=:parentToken");
                    parameters.put("parentToken", parentToken);
                }
                if (nodeId != null) {
                    conditions.add("nodeId=:nodeId");
                    parameters.put("nodeId", nodeId);
                }
                if (finished != null) {
                    if (finished) {
                        conditions.add("subProcess.endDate is not null");
                    } else {
                        conditions.add("subProcess.endDate is null");
                    }
                }
                if (conditions.size() == 0) {
                    throw new IllegalArgumentException("Filter should be specified");
                }
                String hql = "from NodeProcess where " + Joiner.on(" and ").join(conditions) + " order by id asc";
                Query query = session.createQuery(hql);
                for (Entry<String, Object> param : parameters.entrySet()) {
                    query.setParameter(param.getKey(), param.getValue());
                }
                return query.list();
            }
        });
    }

    public void deleteByProcess(Process process) {
        log.debug("deleting subprocess nodes for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from NodeProcess where process=?", process);
    }

    public List<Process> getSubprocesses(Process process) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(process, null, null, null);
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    public List<Process> getSubprocessesRecursive(Process process) {
        List<Process> result = Lists.newArrayList();
        for (Process subprocess : getSubprocesses(process)) {
            result.add(subprocess);
            result.addAll(getSubprocessesRecursive(subprocess));
        }
        return result;
    }

    public List<Process> getSubprocesses(Process process, String nodeId, Token parentToken, Boolean finished) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(process, parentToken, nodeId, finished);
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }
}
