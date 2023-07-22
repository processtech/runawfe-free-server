package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import ru.runa.wfe.audit.aggregated.AssignmentHistory;
import ru.runa.wfe.audit.aggregated.ProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateAggregatedLogsTables extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        createAssignmentHistoryTable();
        createTaskHistoryTable();
        createProcessHistoryTable();
    }

    /**
     * Creates table, indexes e.t.c for AssignmentHistory.
     */
    private void createAssignmentHistoryTable() {
        executeUpdates(
                getDDLCreateTable("BPM_AGGLOG_ASSIGNMENTS", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new CharColumnDef("DISCRIMINATOR", 1).notNull(),
                        new BigintColumnDef("ASSIGNMENT_OBJECT_ID").notNull(),
                        new IntColumnDef("IDX").notNull(),
                        new TimestampColumnDef("ASSIGNMENT_DATE").notNull(),
                        new VarcharColumnDef("OLD_EXECUTOR_NAME", 1024),
                        new VarcharColumnDef("NEW_EXECUTOR_NAME", 1024)
                )),
                getDDLCreateSequence("SEQ_BPM_AGGLOG_ASSIGNMENTS"),
                getDDLCreateIndex("BPM_AGGLOG_ASSIGNMENTS", "IX_AGGLOG_ASSIGN_DATE", "ASSIGNMENT_DATE"),
                getDDLCreateIndex("BPM_AGGLOG_ASSIGNMENTS", "IX_AGGLOG_ASSIGN_OBJECT", "ASSIGNMENT_OBJECT_ID", "IDX"),
                getDDLCreateIndex("BPM_AGGLOG_ASSIGNMENTS", "IX_AGGLOG_ASSIGN_EXECUTOR", "NEW_EXECUTOR_NAME")
        );
    }

    /**
     * Creates table, indexes e.t.c for {@link TaskAggregatedLog}.
     */
    private void createTaskHistoryTable() {
        executeUpdates(
            getDDLCreateTable("BPM_AGGLOG_TASKS", list(
                    new BigintColumnDef("ID").primaryKey(),
                    new BigintColumnDef("TASK_ID").notNull(),
                    new BigintColumnDef("PROCESS_ID").notNull(),
                    new VarcharColumnDef("INITIAL_ACTOR_NAME", 1024),
                    new VarcharColumnDef("COMPLETE_ACTOR_NAME", 1024),
                    new ColumnDef("CREATE_DATE", dialect.getTypeName(Types.DATE)).notNull(),
                    new ColumnDef("DEADLINE_DATE", dialect.getTypeName(Types.DATE)),
                    new ColumnDef("END_DATE", dialect.getTypeName(Types.DATE)),
                    new IntColumnDef("END_REASON").notNull(),
                    new BigintColumnDef("TOKEN_ID").notNull(),
                    new VarcharColumnDef("NODE_ID", 1024),
                    new VarcharColumnDef("TASK_NAME", 1024),
                    new VarcharColumnDef("SWIMLANE_NAME", 1024)
            )),
            getDDLCreateSequence("SEQ_BPM_AGGLOG_TASKS"),
            getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_PROCESS", "PROCESS_ID"),
            getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_CREATE_DATE", "CREATE_DATE"),
            getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_END_DATE", "END_DATE")
        );
    }

    /**
     * Creates table, indexes e.t.c for ProcessAggregatedLog.
     */
    private void createProcessHistoryTable() {
        executeUpdates(
                getDDLCreateTable("BPM_AGGLOG_PROCESS", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new BigintColumnDef("PROCESS_ID").notNull(),
                        new BigintColumnDef("PARENT_PROCESS_ID"),
                        new VarcharColumnDef("START_ACTOR_NAME", 1024),
                        new VarcharColumnDef("CANCEL_ACTOR_NAME", 1024),
                        new ColumnDef("CREATE_DATE", dialect.getTypeName(Types.DATE)).notNull(),
                        new ColumnDef("END_DATE", dialect.getTypeName(Types.DATE)),
                        new IntColumnDef("END_REASON")
                )),
                getDDLCreateSequence("SEQ_BPM_AGGLOG_PROCESS"),
                getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_INSTANCE", "PROCESS_ID"),
                getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_CREATE_DATE", "CREATE_DATE"),
                getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_END_DATE", "END_DATE")
        );
    }
}
