package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class EnlargeMessageMaxSizePatch extends DbMigration {

    @Override
    protected void executeDDLBefore() throws Exception {
        executeUpdates(
                getDDLDropColumn("CHAT_MESSAGE", "TEXT"),
                getDDLCreateColumn("CHAT_MESSAGE", new VarcharColumnDef("TEXT", 2048)),
                getDDLCreateColumn("CHAT_MESSAGE", new ClobColumnDef("EXTENDED_TEXT")));
    }
}
