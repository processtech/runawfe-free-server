package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTransitionNameForTaskPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_AGGLOG_TASK", new VarcharColumnDef("TRANSITION_NAME", 1024)));
    }

}
