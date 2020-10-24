package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSubProcessIndexColumn extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_SUBPROCESS", new IntColumnDef("SUBPROCESS_INDEX")));
    }
}
