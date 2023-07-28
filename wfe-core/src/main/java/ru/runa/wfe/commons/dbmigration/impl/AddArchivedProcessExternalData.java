package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddArchivedProcessExternalData extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
            getDDLCreateColumn("archived_process", new BigintColumnDef("external_data")),
            getDDLCreateIndex("archived_process", "ix_arch_process_external_data", "external_data")
        );
    }
}
