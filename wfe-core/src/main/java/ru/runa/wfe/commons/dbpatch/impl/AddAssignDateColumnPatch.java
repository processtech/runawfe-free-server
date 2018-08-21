package ru.runa.wfe.commons.dbpatch.impl;

import com.google.common.base.Objects;
import java.sql.Types;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.ITaskAssignLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.dbpatch.DbPatch;

public class AddAssignDateColumnPatch extends DbPatch {
    @Autowired
    private ProcessLogDao processLogDao;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("ASSIGN_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

    @Override
    public void executeDML(Session session) {
        List<Object[]> rows = session.createSQLQuery("SELECT ID, PROCESS_ID, NODE_ID FROM BPM_TASK").list();
        log.info("Found " + rows.size() + " tasks");
        SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_TASK SET ASSIGN_DATE=:assignDate WHERE ID=:taskId");
        for (Object[] row : rows) {
            Long taskId = ((Number) row[0]).longValue();
            ProcessLogFilter filter = new ProcessLogFilter(((Number) row[1]).longValue());
            filter.setType(IProcessLog.Type.TASK_ASSIGN);
            filter.setNodeId((String) row[2]);
            List<IProcessLog> logs = processLogDao.getAll(filter);
            for (IProcessLog processLog : logs) {
                ITaskAssignLog taskAssignLog = (TaskAssignLog) processLog;
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
