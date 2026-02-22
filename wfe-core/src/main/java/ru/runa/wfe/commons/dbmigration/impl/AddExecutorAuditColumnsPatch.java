package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddExecutorAuditColumnsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
            "ALTER TABLE SYSTEM_LOG ADD COLUMN EXECUTOR_ID BIGINT",
            "ALTER TABLE SYSTEM_LOG ADD COLUMN EXECUTOR_NAME VARCHAR(1024)",
            "ALTER TABLE SYSTEM_LOG ADD COLUMN EXECUTOR_TYPE VARCHAR(255)",
            "ALTER TABLE SYSTEM_LOG ADD COLUMN GROUP_ID BIGINT",
            "ALTER TABLE SYSTEM_LOG ADD COLUMN GROUP_NAME VARCHAR(1024)"
        );
    }

    @Override
    protected void executeDDLAfter() {
        // ничего не требуется
    }
}