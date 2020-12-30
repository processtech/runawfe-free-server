package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTransactionalBotSupport extends DbMigration {
    private static final String TABLE_NAME = "BOT";

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn(TABLE_NAME, new BooleanColumnDef("IS_TRANSACTIONAL")),
                getDDLCreateColumn(TABLE_NAME, new BigintColumnDef("TRANSACTIONAL_TIMEOUT")),
                getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_DUE_DATE", dialect.getTypeName(Types.TIMESTAMP))),
                getDDLCreateColumn(TABLE_NAME, new BigintColumnDef("BOUND_PROCESS_ID")),
                getDDLCreateColumn(TABLE_NAME, new VarcharColumnDef("BOUND_SUBPROCESS_ID", 1024))
        );
    }
}
