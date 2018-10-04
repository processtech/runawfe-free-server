package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTransactionalBotSupport extends DbMigration {
    private static final String TABLE_NAME = "BOT";

    @Override
    protected void executeDDLBefore() {
        executeDDL(
                getDDLCreateColumn(TABLE_NAME, new ColumnDef("IS_TRANSACTIONAL", dialect.getTypeName(Types.BIT))),
                getDDLCreateColumn(TABLE_NAME, new ColumnDef("TRANSACTIONAL_TIMEOUT", dialect.getTypeName(Types.BIGINT))),
                getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_DUE_DATE", dialect.getTypeName(Types.TIMESTAMP))),
                getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_PROCESS_ID", dialect.getTypeName(Types.BIGINT))),
                getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_SUBPROCESS_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)))
        );
    }
}
