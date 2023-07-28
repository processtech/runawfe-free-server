package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateEventSubprocessTriggerTable extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("BPM_EVENT_SUBPROCESS_TRIG", list(
                    new BigintColumnDef("id").primaryKey(),
                    new BigintColumnDef("process_id").notNull(),
                    new VarcharColumnDef("node_id", 1024),
                    new VarcharColumnDef("message_selector", 1024)
            )),
                getDDLCreateSequence("SEQ_BPM_EVENT_SUBPROCESS_TRIG")
        );
    }
}
