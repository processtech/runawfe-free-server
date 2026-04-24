package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class ExpandSignalListenerEventTypePatch extends DbMigration  {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLModifyColumn("BPM_AGGLOG_SIGNAL_LISTENER",
                        new VarcharColumnDef("EVENT_TYPE", 16)
                )
        );
    }
}
