package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.dbpatch.DBPatch;

import com.google.common.base.Objects;

public class AddAssignDateColumnPatch extends DBPatch {
    @Autowired
    private ProcessLogDao processLogDAO;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("ASSIGN_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        List<Object[]> rows = session.createSQLQuery("SELECT ID, PROCESS_ID, NODE_ID FROM BPM_TASK").list();
        log.info("Found " + rows.size() + " tasks");
        SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_TASK SET ASSIGN_DATE=:assignDate WHERE ID=:taskId");
        for (Object[] row : rows) {
            Long taskId = ((Number) row[0]).longValue();
            ProcessLogFilter filter = new ProcessLogFilter(((Number) row[1]).longValue());
            filter.setRootClassName(TaskAssignLog.class.getName());
            filter.setNodeId((String) row[2]);
            List<ProcessLog> logs = processLogDAO.getAll(filter);
            for (ProcessLog processLog : logs) {
                TaskAssignLog taskAssignLog = (TaskAssignLog) processLog;
                if (Objects.equal(taskId, taskAssignLog.getTaskId())) {
                    updateQuery.setParameter("assignDate", taskAssignLog.getCreateDate());
                    updateQuery.setParameter("taskId", taskId);
                    updateQuery.executeUpdate();
                    break;
                }
            }
        }
    }

}