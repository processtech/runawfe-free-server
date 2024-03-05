package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class ExecutorIfFullNameIsNullOrEmptySetToName extends DbMigration {
    @Override
    public void executeDML(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE EXECUTOR SET FULL_NAME = NAME WHERE FULL_NAME IS NULL OR FULL_NAME = '' OR FULL_NAME = ' '");
        }
    }

    @Override
    protected void executeDDLAfter() throws Exception {
        executeUpdates(getDDLModifyColumnNullability("EXECUTOR", new VarcharColumnDef("full_name", 1024).notNull()));
    }
}
