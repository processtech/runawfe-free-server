package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddTransactionalBotSupport extends DBPatch {
    private static final String TABLE_NAME = "BOT";

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn(TABLE_NAME, new ColumnDef("IS_TRANSACTIONAL", dialect.getTypeName(Types.BIT))));
        sql.add(getDDLCreateColumn(TABLE_NAME, new ColumnDef("TRANSACTIONAL_TIMEOUT", dialect.getTypeName(Types.BIGINT))));
        sql.add(getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_DUE_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        sql.add(getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_PROCESS_ID", dialect.getTypeName(Types.BIGINT))));
        sql.add(getDDLCreateColumn(TABLE_NAME, new ColumnDef("BOUND_SUBPROCESS_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        return sql;
    }

}
