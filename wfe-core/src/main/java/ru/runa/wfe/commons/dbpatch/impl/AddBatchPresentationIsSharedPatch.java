package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddBatchPresentationIsSharedPatch extends DBPatch {
    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("IS_SHARED", dialect.getTypeName(Types.BIT), true)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        session.createSQLQuery("UPDATE BATCH_PRESENTATION SET IS_SHARED = FALSE").executeUpdate();
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLModifyColumnNullable("BATCH_PRESENTATION", "IS_SHARED", false, dialect.getTypeName(Types.BIT)));
        sql.add(getDDLCreateIndex("BATCH_PRESENTATION", "IX_BATCH_PRESENTATION_SHARED", "IS_SHARED"));
        return sql;
    }
}
