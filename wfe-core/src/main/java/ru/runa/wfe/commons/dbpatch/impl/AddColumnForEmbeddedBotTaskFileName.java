package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbpatch.DbPatch;

public class AddColumnForEmbeddedBotTaskFileName extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BOT_TASK", new ColumnDef("EMBEDDED_FILE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        return sql;
    }

}
