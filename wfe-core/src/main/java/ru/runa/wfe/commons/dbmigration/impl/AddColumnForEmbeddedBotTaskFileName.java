package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddColumnForEmbeddedBotTaskFileName extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BOT_TASK", new VarcharColumnDef("EMBEDDED_FILE_NAME", 1024)));
    }
}
