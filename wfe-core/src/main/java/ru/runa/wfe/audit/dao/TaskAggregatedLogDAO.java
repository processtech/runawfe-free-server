package ru.runa.wfe.audit.dao;

import java.util.List;

import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.commons.dao.GenericDAO;

public class TaskAggregatedLogDAO extends GenericDAO<TaskAggregatedLog> {

    public TaskAggregatedLog getTaskLog(long taskId) {
        String query = "from TaskAggregatedLog where taskId=? order by taskId desc";
        List<TaskAggregatedLog> existing = getHibernateTemplate().find(query, taskId);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0);
        }
        return null;
    }
	
}
