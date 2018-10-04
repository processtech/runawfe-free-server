package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * 
 * @author artmikheev
 * 
 */
public class AddEmbeddedFileForBotTask extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeDDL(getDDLCreateColumn("BOT_TASK", new ColumnDef("EMBEDDED_FILE", Types.BLOB)));
    }
}
