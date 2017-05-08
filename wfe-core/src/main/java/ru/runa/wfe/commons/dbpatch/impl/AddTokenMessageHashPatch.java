package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddTokenMessageHashPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("MESSAGE_HASH", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        sql.add(getDDLCreateIndex("BPM_TOKEN", "IX_MESSAGE_HASH", "MESSAGE_HASH"));
        return sql;
    }

    @Override
    public void applyPatch(Session session) throws Exception {
    }
}
