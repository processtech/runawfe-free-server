package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * https://sourceforge.net/p/runawfe/bugs/378/
 * 
 * @author Dofs
 * @since 4.1.0
 */
public class TaskOpenedByExecutorsPatch extends DbMigration {

    @Autowired
    private ExecutorDao executorDao;

    @Override
    protected void executeDDLAfter() {
        executeUpdates(getDDLDropColumn("BPM_TASK", "FIRST_OPEN"));
    }

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("BPM_TASK_OPENED", list(
                        new BigintColumnDef("TASK_ID").notNull(),
                        new BigintColumnDef("EXECUTOR_ID").notNull()
                )),
                getDDLCreateForeignKey("BPM_TASK_OPENED", "FK_TASK_OPENED_TASK", "TASK_ID", "BPM_TASK", "ID")
        );
    }

    @Override
    public void executeDML(Session session) {
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
