package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddTitleAndDepartmentColumnsToActorPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("EXECUTOR", new VarcharColumnDef("TITLE", 1024)),
                getDDLCreateColumn("EXECUTOR", new VarcharColumnDef("DEPARTMENT", 1024)),
                getDDLModifyColumn("EXECUTOR", new VarcharColumnDef("PHONE", 1024))
        );
    }
}
