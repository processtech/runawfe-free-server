package ru.runa.wfe.job.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class UnusedTemporaryGroupsCleaner {
    protected final Log log = LogFactory.getLog(getClass());
    // @Autowired
    // protected HibernateQueryFactory queryFactory;
    @Autowired
    protected SessionFactory sessionFactory;

    @Transactional
    public void execute() {
        log.debug("Cleaning unused temporary groups");
        // TODO seems like bug in generated query: Column "TEMPORARYG0_.PROCESS_ID" not found
        // QTemporaryGroup tg = QTemporaryGroup.temporaryGroup;
        // QSwimlane s = QSwimlane.swimlane;
        // QTask t = QTask.task;
        // queryFactory
        // .delete(tg)
        // .where(tg.processId.notIn(JPAExpressions.selectDistinct(s.process.id).from(s)).and(
        // tg.processId.notIn(JPAExpressions.selectDistinct(t.process.id).from(t)))).execute();
        //
        // This is not working with same error
        // sessionFactory.getCurrentSession()
        // .createQuery("delete from TemporaryGroup where processId not in (select process.id from Swimlane union select process.id from Task)")
        // .executeUpdate();
        //
        sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                        "DELETE FROM EXECUTOR_GROUP_MEMBER WHERE GROUP_ID IN (SELECT ID FROM EXECUTOR WHERE DISCRIMINATOR IN ('D', 'T', 'E') AND ID NOT IN (SELECT DISTINCT(EXECUTOR_ID) FROM BPM_SWIMLANE WHERE EXECUTOR_ID IS NOT NULL UNION SELECT DISTINCT(EXECUTOR_ID) FROM BPM_TASK WHERE EXECUTOR_ID IS NOT NULL))")
                .executeUpdate();
        sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                        "DELETE FROM EXECUTOR WHERE DISCRIMINATOR IN ('D', 'T', 'E') AND ID NOT IN (SELECT DISTINCT(EXECUTOR_ID) FROM BPM_SWIMLANE WHERE EXECUTOR_ID IS NOT NULL UNION SELECT DISTINCT(EXECUTOR_ID) FROM BPM_TASK WHERE EXECUTOR_ID IS NOT NULL)")
                .executeUpdate();
    }

}
