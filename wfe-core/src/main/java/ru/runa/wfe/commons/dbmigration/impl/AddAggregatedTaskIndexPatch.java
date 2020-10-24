package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddAggregatedTaskIndexPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_AGGLOG_TASKS", new IntColumnDef("TASK_INDEX")));
    }
}
