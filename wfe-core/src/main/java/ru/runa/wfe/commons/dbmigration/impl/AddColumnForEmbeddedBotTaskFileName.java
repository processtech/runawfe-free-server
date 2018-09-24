package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddColumnForEmbeddedBotTaskFileName extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BOT_TASK", new ColumnDef("EMBEDDED_FILE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
    }
}
