package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddStartProcessTimerJobRefactorRm2681 extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLRenameColumn("bpm_job", "definition_version_id", new BigintColumnDef("definition_id"))
        );
    }
}
