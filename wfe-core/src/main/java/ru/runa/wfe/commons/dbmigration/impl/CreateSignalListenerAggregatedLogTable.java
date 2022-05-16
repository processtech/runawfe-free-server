package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.audit.aggregated.SignalListenerAggregatedLog;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateSignalListenerAggregatedLogTable extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        createSignalListenerAggregatedLogTable();
    }

    /**
     * Creates table, indexes e.t.c for {@link SignalListenerAggregatedLog}.
     */
    private void createSignalListenerAggregatedLogTable() {
        executeUpdates(
                getDDLCreateTable("BPM_AGGLOG_SIGNAL_LISTENER", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new BigintColumnDef("PROCESS_ID").notNull(),
                        new BigintColumnDef("TOKEN_ID").notNull(),
                        new VarcharColumnDef("NODE_ID", 32).notNull(),
                        new VarcharColumnDef("NODE_NAME", 1024).notNull(),
                        new VarcharColumnDef("EVENT_TYPE", 8).notNull(),
                        new TimestampColumnDef("CREATE_DATE").notNull(),
                        new TimestampColumnDef("EXECUTE_DATE")
                        )),
                getDDLCreateSequence("SEQ_BPM_AGGLOG_SIGNAL_LISTENER"),
                getDDLCreateIndex("BPM_AGGLOG_SIGNAL_LISTENER", "IX_AGGLOG_SL_PROCESS", "PROCESS_ID"),
                getDDLCreateIndex("BPM_AGGLOG_SIGNAL_LISTENER", "IX_AGGLOG_SL_CREATE_DATE", "CREATE_DATE"),
                getDDLCreateIndex("BPM_AGGLOG_SIGNAL_LISTENER", "IX_AGGLOG_SL_END_DATE", "EXECUTE_DATE")
        );
    }
}
