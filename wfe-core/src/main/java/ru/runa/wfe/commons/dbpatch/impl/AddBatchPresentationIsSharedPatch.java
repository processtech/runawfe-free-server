package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddBatchPresentationIsSharedPatch extends DBPatch {
    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("SHARED", dialect.getTypeName(Types.BIT), true)));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        String initialValue = dbType == DBType.ORACLE ? "0" : "FALSE";
        session.createSQLQuery("UPDATE BATCH_PRESENTATION SET SHARED = " + initialValue).executeUpdate();
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLModifyColumnNullability("BATCH_PRESENTATION", "SHARED", dialect.getTypeName(Types.BIT), false));
        return sql;
    }
}
