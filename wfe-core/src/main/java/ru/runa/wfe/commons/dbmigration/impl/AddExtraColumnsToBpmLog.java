package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddExtraColumnsToBpmLog extends DbMigration  {

    private static final String BPM_LOG = "BPM_LOG";
    private static final String ARCHIVED_LOG = "ARCHIVED_LOG";

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn(BPM_LOG, new VarcharColumnDef("NODE_NAME", 1024)),
                getDDLCreateColumn(BPM_LOG, new VarcharColumnDef("EXECUTOR_NAME", 1024)),
                getDDLCreateColumn(BPM_LOG, new VarcharColumnDef("SWIMLANE_NAME", 1024)),
                getDDLCreateColumn(BPM_LOG, new VarcharColumnDef("VARIABLE_NAME", 1024)),
                getDDLCreateColumn(BPM_LOG, new IntColumnDef("TASK_ID"))
        );
        executeUpdates(getDDLCreateColumn(ARCHIVED_LOG, new VarcharColumnDef("NODE_NAME", 1024)),
                getDDLCreateColumn(ARCHIVED_LOG, new VarcharColumnDef("EXECUTOR_NAME", 1024)),
                getDDLCreateColumn(ARCHIVED_LOG, new VarcharColumnDef("SWIMLANE_NAME", 1024)),
                getDDLCreateColumn(ARCHIVED_LOG, new VarcharColumnDef("VARIABLE_NAME", 1024)),
                getDDLCreateColumn(ARCHIVED_LOG, new IntColumnDef("TASK_ID")));
    }
    
}
