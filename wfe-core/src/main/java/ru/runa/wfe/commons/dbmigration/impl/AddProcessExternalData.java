package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddProcessExternalData extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
            getDDLCreateColumn("bpm_process", new BigintColumnDef("external_data")),
            getDDLCreateIndex("bpm_process", "ix_process_external_data", "external_data")
        );
    }
}
