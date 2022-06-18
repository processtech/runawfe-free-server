package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddArchivedTokenNodeNameAndNodeEnterDateColumnsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("ARCHIVED_TOKEN", new VarcharColumnDef("NODE_NAME", 1024)),
                getDDLCreateColumn("ARCHIVED_TOKEN", new TimestampColumnDef("NODE_ENTER_DATE")));
    }

}
