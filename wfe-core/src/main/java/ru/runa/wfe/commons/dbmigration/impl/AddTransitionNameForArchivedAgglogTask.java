package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTransitionNameForArchivedAgglogTask extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("ARCHIVED_AGGLOG_TASK", new VarcharColumnDef("TRANSITION_NAME", 1024)));
    }

}
