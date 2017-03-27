package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddTitleAndDepartmentColumnsToActorPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("TITLE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("DEPARTMENT", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        sql.add(getDDLModifyColumn("EXECUTOR", "PHONE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }

}
