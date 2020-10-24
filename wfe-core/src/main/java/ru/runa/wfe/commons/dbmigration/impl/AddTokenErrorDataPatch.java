package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTokenErrorDataPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_TOKEN", new TimestampColumnDef("ERROR_DATE")),
                getDDLCreateColumn("BPM_TOKEN", new VarcharColumnDef("ERROR_MESSAGE", 1024))
        );
    }
}
