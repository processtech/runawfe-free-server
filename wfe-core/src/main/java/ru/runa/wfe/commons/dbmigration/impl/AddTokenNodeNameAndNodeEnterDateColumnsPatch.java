package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTokenNodeNameAndNodeEnterDateColumnsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_TOKEN", new VarcharColumnDef("NODE_NAME", 1024)),
                getDDLCreateColumn("BPM_TOKEN", new TimestampColumnDef("NODE_ENTER_DATE")));
    }

}
