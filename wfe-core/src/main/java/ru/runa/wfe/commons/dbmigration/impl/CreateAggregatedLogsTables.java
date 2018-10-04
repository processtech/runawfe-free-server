package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
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
     * Creates table, indexes e.t.c for {@link AssignmentHistory}.
     */
    private void createAssignmentHistoryTable() {
        executeDDL(
                getDDLCreateTable("BPM_AGGLOG_ASSIGNMENTS", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("DISCRIMINATOR", dialect.getTypeName(Types.CHAR, 1, 1, 1), false),
                        new ColumnDef("ASSIGNMENT_OBJECT_ID", dialect.getTypeName(Types.BIGINT), false),
                        new ColumnDef("IDX", dialect.getTypeName(Types.INTEGER), false),
                        new ColumnDef("ASSIGNMENT_DATE", dialect.getTypeName(Types.DATE), false),
                        new ColumnDef("OLD_EXECUTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                        new ColumnDef("NEW_EXECUTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)
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
        executeDDL(
            getDDLCreateTable("BPM_AGGLOG_TASKS", list(
                    new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                    new ColumnDef("TASK_ID", dialect.getTypeName(Types.BIGINT), false),
                    new ColumnDef("PROCESS_ID", dialect.getTypeName(Types.BIGINT), false),
                    new ColumnDef("INITIAL_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                    new ColumnDef("COMPLETE_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                    new ColumnDef("CREATE_DATE", dialect.getTypeName(Types.DATE), false),
                    new ColumnDef("DEADLINE_DATE", dialect.getTypeName(Types.DATE), true),
                    new ColumnDef("END_DATE", dialect.getTypeName(Types.DATE), true),
                    new ColumnDef("END_REASON", dialect.getTypeName(Types.INTEGER), false),
                    new ColumnDef("TOKEN_ID", dialect.getTypeName(Types.BIGINT), false),
                    new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                    new ColumnDef("TASK_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                    new ColumnDef("SWIMLANE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)
            )),
            getDDLCreateSequence("SEQ_BPM_AGGLOG_TASKS"),
            getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_PROCESS", "PROCESS_ID"),
            getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_CREATE_DATE", "CREATE_DATE"),
            getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_END_DATE", "END_DATE")
        );
    }

    /**
     * Creates table, indexes e.t.c for {@link ProcessInstanceAggregatedLog}.
     */
    private void createProcessHistoryTable() {
        executeDDL(
                getDDLCreateTable("BPM_AGGLOG_PROCESS", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("PROCESS_ID", dialect.getTypeName(Types.BIGINT), false),
                        new ColumnDef("PARENT_PROCESS_ID", dialect.getTypeName(Types.BIGINT), true),
                        new ColumnDef("START_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                        new ColumnDef("CANCEL_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true),
                        new ColumnDef("CREATE_DATE", dialect.getTypeName(Types.DATE), false),
                        new ColumnDef("END_DATE", dialect.getTypeName(Types.DATE), true),
                        new ColumnDef("END_REASON", dialect.getTypeName(Types.INTEGER), false)
                )),
                getDDLCreateSequence("SEQ_BPM_AGGLOG_PROCESS"),
                getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_INSTANCE", "PROCESS_ID"),
                getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_CREATE_DATE", "CREATE_DATE"),
                getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_END_DATE", "END_DATE")
        );
    }
}
