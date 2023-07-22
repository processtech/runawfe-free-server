package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.audit.aggregated.TimerAggregatedLog;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateTimerAggregatedLogTable extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        createTimerAggregateLogTable();
    }

    /**
     * Creates table, indexes e.t.c for {@link TimerAggregatedLog}.
     */
    private void createTimerAggregateLogTable() {
        executeUpdates(
                getDDLCreateTable("BPM_AGGLOG_TIMER", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new BigintColumnDef("PROCESS_ID").notNull(),
                        new BigintColumnDef("TOKEN_ID").notNull(),
                        new VarcharColumnDef("NODE_ID", 1024),
                        new VarcharColumnDef("NODE_NAME", 1024),
                        new TimestampColumnDef("CREATE_DATE").notNull(),
                        new TimestampColumnDef("END_DATE"),
                        new TimestampColumnDef("DUE_DATE")
                )),
                getDDLCreateSequence("SEQ_BPM_AGGLOG_TIMER"),
                getDDLCreateIndex("BPM_AGGLOG_TIMER", "IX_AGGLOG_TIMER_PROCESS", "PROCESS_ID"),
                getDDLCreateIndex("BPM_AGGLOG_TIMER", "IX_AGGLOG_TIMER_CREATE_DATE", "CREATE_DATE"),
                getDDLCreateIndex("BPM_AGGLOG_TIMER", "IX_AGGLOG_TIMER_END_DATE", "END_DATE")
        );
    }
}
