package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddDueDateExpressionToJobAndTask extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_JOB", new VarcharColumnDef("DUE_DATE_EXPRESSION", 1024)),
                getDDLCreateColumn("BPM_TASK", new VarcharColumnDef("DEADLINE_DATE_EXPRESSION", 1024))
        );
    }
}
