package ru.runa.wfe.commons.dbmigration.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.dao.ExecutorDao;

public class CreateAdminScriptTables extends DbMigration {

    @Autowired
    protected ExecutorDao executorDao;
    @Autowired
    protected PermissionDao permissionDao;

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
