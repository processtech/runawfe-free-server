package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddColumnForEmbeddedBotTaskFileName extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BOT_TASK", new ColumnDef("EMBEDDED_FILE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }

}
