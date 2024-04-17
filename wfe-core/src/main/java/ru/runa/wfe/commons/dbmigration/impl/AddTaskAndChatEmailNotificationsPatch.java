package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTaskAndChatEmailNotificationsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("EXECUTOR", new BooleanColumnDef("TASK_EMAIL_NOTIFICATIONS")),
                getDDLCreateColumn("EXECUTOR", new BooleanColumnDef("CHAT_EMAIL_NOTIFICATIONS")));
    }

    @Override
    public void executeDML(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE " + schemaPrefix + "EXECUTOR SET TASK_EMAIL_NOTIFICATIONS = " + getBooleanValue(true)
                    + " WHERE TASK_EMAIL_NOTIFICATIONS IS NULL");
            stmt.executeUpdate("UPDATE " + schemaPrefix + "EXECUTOR SET CHAT_EMAIL_NOTIFICATIONS = " + getBooleanValue(true)
                    + " WHERE CHAT_EMAIL_NOTIFICATIONS IS NULL");
        }
    }
}
