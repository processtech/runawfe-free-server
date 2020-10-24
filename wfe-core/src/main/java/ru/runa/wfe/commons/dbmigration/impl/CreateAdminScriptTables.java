package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateAdminScriptTables extends DbMigration {
    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("ADMIN_SCRIPT", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new VarcharColumnDef("NAME", 1024).notNull(),
                        new BlobColumnDef("CONTENT").notNull()
                )),
                getDDLCreateSequence("SEQ_ADMIN_SCRIPT"),
                getDDLCreateUniqueKey("ADMIN_SCRIPT", "IX_ADMIN_SCRIPT_NAME", "NAME")
        );
    }
}
