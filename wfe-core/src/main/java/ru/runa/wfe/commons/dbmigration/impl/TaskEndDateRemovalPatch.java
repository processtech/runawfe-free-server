package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class TaskEndDateRemovalPatch extends DbMigration {

    @Override
    public void executeDML(Session session) {
        log.info("Deleted completed tasks: " + session.createSQLQuery("DELETE FROM BPM_TASK WHERE END_DATE IS NOT NULL").executeUpdate());
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(getDDLDropColumn("BPM_TASK", "END_DATE"));
    }
}
