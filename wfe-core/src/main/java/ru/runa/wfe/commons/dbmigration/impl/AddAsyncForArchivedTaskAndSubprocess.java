package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddAsyncForArchivedTaskAndSubprocess extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("ARCHIVED_SUBPROCESS", new BooleanColumnDef("ASYNC")));
    }

}
