package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTitleAndDepartmentColumnsToActorPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("EXECUTOR", new ColumnDef("TITLE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))),
                getDDLCreateColumn("EXECUTOR", new ColumnDef("DEPARTMENT", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))),
                getDDLModifyColumn("EXECUTOR", "PHONE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))
        );
    }
}
