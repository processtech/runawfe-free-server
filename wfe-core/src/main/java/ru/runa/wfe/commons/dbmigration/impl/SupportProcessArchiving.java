package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * See TNMS #5006.
 * <p>
 * "Not null" constraints, indexes and foreign keys for ARCHIVED_* tables are copy-pasted from current BPM_* tables,
 * except those which may result in FK violation during batch insert-select in ProcessArchiver.
 */
public class SupportProcessArchiving extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                // Nullable per-definition configuration; default is SystemProperties.getProcessDefaultSecondsBeforeArchiving():
                getDDLCreateColumn("bpm_process_definition", new IntColumnDef("seconds_before_archiving")),

                // Process: all fields except EXECUTION_STATUS.
                getDDLCreateTable("archived_process", list(
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new BigintColumnDef("parent_id"),
                        new TimestampColumnDef("end_date"),
                        new TimestampColumnDef("start_date"),
                        new VarcharColumnDef("tree_path", 1024),
                        new BigintColumnDef("version"),
                        new BigintColumnDef("definition_version_id").notNull(),
                        new BigintColumnDef("root_token_id").notNull()
                )),

                // Token: all fields except EXECUTION_STATUS:
                getDDLCreateTable("archived_token", list(
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new VarcharColumnDef("error_message", 1024),
                        new VarcharColumnDef("transition_id", 1024),
                        new VarcharColumnDef("message_selector", 1024),
                        new TimestampColumnDef("error_date"),
                        new TimestampColumnDef("end_date"),
                        new TimestampColumnDef("start_date"),
                        new VarcharColumnDef("node_id", 1024),
                        new BooleanColumnDef("reactivate_parent"),
                        new VarcharColumnDef("node_type", 1024),
                        new BigintColumnDef("version"),
                        new VarcharColumnDef("name", 1024),
                        new BigintColumnDef("process_id"),
                        new BigintColumnDef("parent_id")
                )),

                // NodeProcess: all fields.
                getDDLCreateTable("archived_subprocess", list(
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new TimestampColumnDef("create_date").notNull(),
                        new VarcharColumnDef("parent_node_id", 1024),
                        new IntColumnDef("subprocess_index"),
                        new BigintColumnDef("parent_token_id"),
                        new BigintColumnDef("parent_process_id").notNull(),
                        new BigintColumnDef("process_id").notNull(),
                        new BigintColumnDef("root_process_id").notNull()
                )),

                // Swimlane: all fields.
                getDDLCreateTable("archived_swimlane", list(
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new TimestampColumnDef("create_date").notNull(),
                        new BigintColumnDef("version"),
                        new VarcharColumnDef("name", 1024),
                        new BigintColumnDef("process_id"),
                        new BigintColumnDef("executor_id")
                )),

                // Variable: all fields.
                getDDLCreateTable("archived_variable", list(
                        new CharColumnDef("discriminator", 1).notNull(),
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new TimestampColumnDef("create_date").notNull(),
                        new VarcharColumnDef("stringvalue", 1024),
                        new CharColumnDef("converter", 1),
                        new BigintColumnDef("version"),
                        new VarcharColumnDef("name", 1024),
                        new TimestampColumnDef("datevalue"),
                        new BlobColumnDef("bytes"),
                        new BigintColumnDef("longvalue"),
                        new DoubleColumnDef("doublevalue"),
                        new BigintColumnDef("process_id").notNull()
                )),

                // ProcessLog: all fields.
                getDDLCreateTable("archived_log", list(
                        new CharColumnDef("discriminator", 1).notNull(),
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new VarcharColumnDef("severity", 1024).notNull(),
                        new BigintColumnDef("token_id"),
                        new TimestampColumnDef("create_date").notNull(),
                        new VarcharColumnDef("node_id", 1024),
                        new BigintColumnDef("process_id").notNull(),
                        new BlobColumnDef("bytes"),
                        new VarcharColumnDef("content", 4000)
                )),

                // TaskAggregatedLog: all fields.
                getDDLCreateTable("archived_agglog_task", list(
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new VarcharColumnDef("initial_actor_name", 1024),
                        new VarcharColumnDef("complete_actor_name", 1024),
                        new VarcharColumnDef("end_reason", 16),
                        new VarcharColumnDef("swimlane_name", 1024),
                        new BigintColumnDef("token_id").notNull(),
                        new VarcharColumnDef("task_name", 1024).notNull(),
                        new BigintColumnDef("task_id").notNull(),
                        new TimestampColumnDef("create_date").notNull(),
                        new TimestampColumnDef("end_date"),
                        new TimestampColumnDef("deadline_date"),
                        new VarcharColumnDef("node_id", 1024).notNull(),
                        new IntColumnDef("task_index"),
                        new BigintColumnDef("process_id").notNull()
                )),

                // TaskAssignmentAggregatedLog: all fields.
                getDDLCreateTable("archived_agglog_assignment", list(
                        new BigintColumnDef("id").primaryKeyNoAutoInc(),
                        new VarcharColumnDef("new_executor_name", 1024),
                        new VarcharColumnDef("old_executor_name", 1024),
                        new TimestampColumnDef("assignment_date").notNull(),
                        new BigintColumnDef("agglog_task_id").notNull()
                )),

                getDDLCreateIndex     ("archived_process", "ix_arch_process_def_ver", "definition_version_id"),
                getDDLCreateForeignKey("archived_process", "fk_arch_process_def_ver", "definition_version_id", "bpm_process_definition_ver", "id"),
                getDDLCreateIndex     ("archived_process", "ix_arch_process_root_token", "root_token_id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_process", "fk_arch_process_root_token", "root_token_id", "archived_token", "id")

                getDDLCreateIndex     ("archived_token", "ix_arch_message_selector", "message_selector"),
                getDDLCreateIndex     ("archived_token", "ix_arch_token_parent", "parent_id"),
                getDDLCreateIndex     ("archived_token", "ix_arch_token_process", "process_id"),
                getDDLCreateForeignKey("archived_token", "fk_arch_token_process", "process_id", "archived_process", "id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_token", "fk_arch_token_parent", "parent_id", "archived_token", "id")

                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_process", "process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_process", "process_id", "archived_process", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_parent", "parent_process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_parent", "parent_process_id", "archived_process", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_root", "root_process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_root", "root_process_id", "archived_process", "id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_token", "parent_token_id", "archived_token", "id")

                getDDLCreateIndex     ("archived_swimlane", "ix_arch_swimlane_process", "process_id"),
                getDDLCreateForeignKey("archived_swimlane", "fk_arch_swimlane_process", "process_id", "archived_process", "id"),
                getDDLCreateForeignKey("archived_swimlane", "fk_arch_swimlane_executor", "executor_id", "executor", "id"),

                getDDLCreateIndex     ("archived_variable", "ix_arch_variable_name", "name"),
                getDDLCreateIndex     ("archived_variable", "ix_arch_variable_process", "process_id"),
                getDDLCreateForeignKey("archived_variable", "fk_arch_variable_process", "process_id", "archived_process", "id"),
                getDDLCreateUniqueKey ("archived_variable", "uk_arch_variable_process", "process_id", "name"),

                getDDLCreateIndex("archived_log", "ix_arch_log_process", "process_id"),

                getDDLCreateIndex("archived_agglog_task", "ix_arch_agglog_task_create", "create_date"),
                getDDLCreateIndex("archived_agglog_task", "ix_arch_agglog_task_end", "end_date"),
                getDDLCreateIndex("archived_agglog_task", "ix_arch_agglog_task_pid", "process_id"),

                getDDLCreateIndex     ("archived_agglog_assignment", "ix_arch_agglog_assign_create", "assignment_date"),
                getDDLCreateIndex     ("archived_agglog_assignment", "ix_arch_agglog_assign_newexec", "new_executor_name"),
                getDDLCreateForeignKey("archived_agglog_assignment", "fk_arch_agglog_assign_task", "agglog_task_id", "archived_agglog_task", "id")
        );
    }
}
