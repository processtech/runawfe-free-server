package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import lombok.val;
import ru.runa.wfe.audit.aggregated.AssignmentHistory;
import ru.runa.wfe.audit.aggregated.ProcessInstanceAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class CreateAggregatedLogsTables extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();

        sql.addAll(createAssignmentHistoryTable());
        sql.addAll(createTaskHistoryTable());
        sql.addAll(createProcessHistoryTable());
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link AssignmentHistory}.
     * 
     * @return Returns list of sql commands for history table creation.
     */
    private List<String> createAssignmentHistoryTable() {
        val sql = new LinkedList<String>();
        val columns = new LinkedList<ColumnDef>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("DISCRIMINATOR", dialect.getTypeName(Types.CHAR, 1, 1, 1), false));
        columns.add(new ColumnDef("ASSIGNMENT_OBJECT_ID", dialect.getTypeName(Types.BIGINT), false));
        columns.add(new ColumnDef("IDX", dialect.getTypeName(Types.INTEGER), false));
        columns.add(new ColumnDef("ASSIGNMENT_DATE", dialect.getTypeName(Types.DATE), false));
        columns.add(new ColumnDef("OLD_EXECUTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("NEW_EXECUTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        sql.add(getDDLCreateTable("BPM_AGGLOG_ASSIGNMENTS", columns, null));

        sql.add(getDDLCreateSequence("SEQ_BPM_AGGLOG_ASSIGNMENTS"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_ASSIGNMENTS", "IX_AGGLOG_ASSIGN_DATE", "ASSIGNMENT_DATE"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_ASSIGNMENTS", "IX_AGGLOG_ASSIGN_OBJECT", "ASSIGNMENT_OBJECT_ID", "IDX"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_ASSIGNMENTS", "IX_AGGLOG_ASSIGN_EXECUTOR", "NEW_EXECUTOR_NAME"));
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link TaskAggregatedLog}.
     * 
     * @return Returns list of sql commands for history table creation.
     */
    private List<String> createTaskHistoryTable() {
        val sql = new LinkedList<String>();
        val columns = new LinkedList<ColumnDef>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("TASK_ID", dialect.getTypeName(Types.BIGINT), false));
        columns.add(new ColumnDef("PROCESS_ID", dialect.getTypeName(Types.BIGINT), false));
        columns.add(new ColumnDef("INITIAL_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("COMPLETE_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("CREATE_DATE", dialect.getTypeName(Types.DATE), false));
        columns.add(new ColumnDef("DEADLINE_DATE", dialect.getTypeName(Types.DATE), true));
        columns.add(new ColumnDef("END_DATE", dialect.getTypeName(Types.DATE), true));
        columns.add(new ColumnDef("END_REASON", dialect.getTypeName(Types.INTEGER), false));
        columns.add(new ColumnDef("TOKEN_ID", dialect.getTypeName(Types.BIGINT), false));
        columns.add(new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("TASK_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("SWIMLANE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        sql.add(getDDLCreateTable("BPM_AGGLOG_TASKS", columns, null));

        sql.add(getDDLCreateSequence("SEQ_BPM_AGGLOG_TASKS"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_CREATE_DATE", "CREATE_DATE"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_TASKS", "IX_AGGLOG_TASKS_END_DATE", "END_DATE"));
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link ProcessInstanceAggregatedLog}.
     * 
     * @return Returns list of sql commands for history table creation.
     */
    private List<String> createProcessHistoryTable() {
        val sql = new LinkedList<String>();
        val columns = new LinkedList<ColumnDef>();
        val id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("PROCESS_ID", dialect.getTypeName(Types.BIGINT), false));
        columns.add(new ColumnDef("PARENT_PROCESS_ID", dialect.getTypeName(Types.BIGINT), true));
        columns.add(new ColumnDef("START_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("CANCEL_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        columns.add(new ColumnDef("CREATE_DATE", dialect.getTypeName(Types.DATE), false));
        columns.add(new ColumnDef("END_DATE", dialect.getTypeName(Types.DATE), true));
        columns.add(new ColumnDef("END_REASON", dialect.getTypeName(Types.INTEGER), false));
        sql.add(getDDLCreateTable("BPM_AGGLOG_PROCESS", columns, null));

        sql.add(getDDLCreateSequence("SEQ_BPM_AGGLOG_PROCESS"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_INSTANCE", "PROCESS_ID"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_CREATE_DATE", "CREATE_DATE"));
        sql.add(getDDLCreateIndex("BPM_AGGLOG_PROCESS", "IX_AGGLOG_PROCESS_END_DATE", "END_DATE"));
        return sql;
    }

}
