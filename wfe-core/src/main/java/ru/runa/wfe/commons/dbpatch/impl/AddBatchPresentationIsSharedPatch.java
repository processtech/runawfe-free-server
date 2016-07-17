package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddBatchPresentationIsSharedPatch extends DBPatch {
    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("SHARED", dialect.getTypeName(Types.BIT), true)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        session.createSQLQuery("UPDATE BATCH_PRESENTATION SET SHARED = 0").executeUpdate();
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLModifyColumnNullability("BATCH_PRESENTATION", "SHARED", dialect.getTypeName(Types.BIT), false));
        return sql;
    }
}
