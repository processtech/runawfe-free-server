package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.Statement;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddStartEventSubprocessTimerJob extends DbMigration {
    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("bpm_job", new VarcharColumnDef("node_id", 1024))
        );
    }

    @Override
    public void executeDML(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + schemaPrefix + "BPM_JOB WHERE DISCRIMINATOR = 'E' AND TIMER_EVENT_TYPE IS NULL");
            stmt.executeUpdate("DELETE FROM " + schemaPrefix + "BPM_JOB WHERE DISCRIMINATOR = 'S' AND TOKEN_ID IS NOT NULL");
        }
    }
}
