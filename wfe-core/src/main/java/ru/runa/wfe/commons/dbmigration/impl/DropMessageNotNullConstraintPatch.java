package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class DropMessageNotNullConstraintPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() throws Exception {
        executeUpdates(getDDLModifyColumnNullability("CHAT_MESSAGE", new VarcharColumnDef("TEXT", 2048)));
    }
}
