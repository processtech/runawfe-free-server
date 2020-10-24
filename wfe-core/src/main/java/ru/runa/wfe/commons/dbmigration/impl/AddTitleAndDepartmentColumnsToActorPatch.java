package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTitleAndDepartmentColumnsToActorPatch extends DbMigration {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("TITLE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("DEPARTMENT", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        sql.add(getDDLModifyColumn("EXECUTOR", "PHONE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        return sql;
    }

}
