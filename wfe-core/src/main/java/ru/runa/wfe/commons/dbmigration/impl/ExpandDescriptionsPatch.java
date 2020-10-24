package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class ExpandDescriptionsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLModifyColumn("BPM_TASK", new VarcharColumnDef("DESCRIPTION", 1024)),
                getDDLModifyColumn("BPM_PROCESS_DEFINITION", new VarcharColumnDef("DESCRIPTION", 1024)),
                getDDLModifyColumn("EXECUTOR_RELATION", new VarcharColumnDef("DESCRIPTION", 1024)),
                getDDLModifyColumn("EXECUTOR", new VarcharColumnDef("DESCRIPTION", 1024))
        );
    }
}
