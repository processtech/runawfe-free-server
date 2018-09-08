package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.dbpatch.DbPatch;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;

import com.google.common.collect.Lists;

/**
 * https://sourceforge.net/p/runawfe/bugs/378/
 * 
 * @author Dofs
 * @since 4.1.0
 */
public class TaskOpenedByExecutorsPatch extends DbPatch {

    @Autowired
    private ExecutorDao executorDao;

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLRemoveColumn("BPM_TASK", "FIRST_OPEN"));
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        List<ColumnDef> columns = Lists.newArrayList();
        columns.add(new ColumnDef("TASK_ID", Types.BIGINT, false));
        columns.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
        sql.add(getDDLCreateTable("BPM_TASK_OPENED", columns, null));
        sql.add(getDDLCreateForeignKey("BPM_TASK_OPENED", "FK_TASK_OPENED_TASK", "TASK_ID", "BPM_TASK", "ID"));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        String q;
        log.info("Processing opened tasks");
        q = "SELECT ID, EXECUTOR_ID FROM BPM_TASK WHERE FIRST_OPEN=0";
        ScrollableResults scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        int processed = 0;
        while (scrollableResults.next()) {
            Long taskId = ((Number) scrollableResults.get(0)).longValue();
            try {
                Long executorId = ((Number) scrollableResults.get(1)).longValue();
                Executor executor = executorDao.getExecutor(executorId);
                if (executor instanceof Actor) {
                    q = "INSERT INTO BPM_TASK_OPENED VALUES (" + taskId + ", " + executorId + ")";
                    session.createSQLQuery(q).executeUpdate();
                    processed++;
                }
            } catch (Exception e) {
                log.warn("For " + taskId + ": " + e);
            }
        }
        log.info("Reverted opened tasks result: " + processed);
    }

}
