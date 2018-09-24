package ru.runa.wfe.commons.dbmigration.impl;

import java.util.List;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * See TNMS #5006.
 * <p>
 * "Not null" constraints, indexes and foreign keys for ARCHIVED_* tables are copy-pasted from current BPM_* tables,
 * except those which may result in FK violation during batch insert-select in ProcessArchiver.
 */
public class SupportProcessArchiving extends DbMigration {

    @Override
    protected List<String> getDDLQueriesBefore() {
        return list(
                // Nullable per-definition configuration; default is SystemProperties.getProcessDefaultSecondsBeforeArchiving():
                getDDLCreateColumn("bpm_process_definition", new IntColumnDef("seconds_before_archiving", true)),

                // Process: all fields except EXECUTION_STATUS.
                getDDLCreateTable("archived_process", list(
                        new BigintColumnDef("id", false),
                        new BigintColumnDef("parent_id", true),
                        new TimestampColumnDef("end_date", true),
                        new TimestampColumnDef("start_date", true),
                        new VarcharColumnDef("tree_path", 1024, true),
                        new BigintColumnDef("version", true),
                        new BigintColumnDef("definition_version_id", false),
                        new BigintColumnDef("root_token_id", false)
                )),

                // Token: all fields except EXECUTION_STATUS:
                getDDLCreateTable("archived_token", list(
                        new BigintColumnDef("id", false),
                        new VarcharColumnDef("error_message", 1024, true),
                        new VarcharColumnDef("transition_id", 1024, true),
                        new VarcharColumnDef("message_selector", 1024, true),
                        new TimestampColumnDef("error_date", true),
                        new TimestampColumnDef("end_date", true),
                        new TimestampColumnDef("start_date", true),
                        new VarcharColumnDef("node_id", 1024, true),
                        new BooleanColumnDef("reactivate_parent", true),
                        new VarcharColumnDef("node_type", 1024, true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new BigintColumnDef("process_id", true),
                        new BigintColumnDef("parent_id", true)
                )),

                // NodeProcess: all fields.
                getDDLCreateTable("archived_subprocess", list(
                        new BigintColumnDef("id", false),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("parent_node_id", 1024, true),
                        new IntColumnDef("subprocess_index", true),
                        new BigintColumnDef("parent_token_id", true),
                        new BigintColumnDef("parent_process_id", false),
                        new BigintColumnDef("process_id", false),
                        new BigintColumnDef("root_process_id", false)
                )),

                // Swimlane: all fields.
                getDDLCreateTable("archived_swimlane", list(
                        new BigintColumnDef("id", false),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new BigintColumnDef("process_id", true),
                        new BigintColumnDef("executor_id", true)
                )),

                // Variable: all fields.
                getDDLCreateTable("archived_variable", list(
                        new CharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("stringvalue", 1024, true),
                        new CharColumnDef("converter", 1, true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new TimestampColumnDef("datevalue", true),
                        new BlobColumnDef("bytes", true),
                        new BigintColumnDef("longvalue", true),
                        new DoubleColumnDef("doublevalue", true),
                        new BigintColumnDef("process_id", false)
                )),

                // ProcessLog: all fields.
                getDDLCreateTable("archived_log", list(
                        new CharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false),
                        new VarcharColumnDef("severity", 1024, false),
                        new BigintColumnDef("token_id", true),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("node_id", 1024, true),
                        new BigintColumnDef("process_id", false),
                        new BlobColumnDef("bytes", true),
                        new VarcharColumnDef("content", 4000, true)
                )),

                getDDLCreatePrimaryKey("archived_process", "pk_archived_process", "id"),
                getDDLCreateIndex     ("archived_process", "ix_arch_process_def_ver", "definition_version_id"),
                getDDLCreateForeignKey("archived_process", "fk_arch_process_def_ver", "definition_version_id", "bpm_process_definition_ver", "id"),
                getDDLCreateIndex     ("archived_process", "ix_arch_process_root_token", "root_token_id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_process", "fk_arch_process_root_token", "root_token_id", "archived_token", "id")

                getDDLCreatePrimaryKey("archived_token", "pk_archived_token", "id"),
                getDDLCreateIndex     ("archived_token", "ix_arch_message_selector", "message_selector"),
                getDDLCreateIndex     ("archived_token", "ix_arch_token_parent", "parent_id"),
                getDDLCreateIndex     ("archived_token", "ix_arch_token_process", "process_id"),
                getDDLCreateForeignKey("archived_token", "fk_arch_token_process", "process_id", "archived_process", "id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_token", "fk_arch_token_parent", "parent_id", "archived_token", "id")

                getDDLCreatePrimaryKey("archived_subprocess", "pk_archived_subprocess", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_process", "process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_process", "process_id", "archived_process", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_parent", "parent_process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_parent", "parent_process_id", "archived_process", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_root", "root_process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_root", "root_process_id", "archived_process", "id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_token", "parent_token_id", "archived_token", "id")

                getDDLCreatePrimaryKey("archived_swimlane", "pk_arch_swimlane", "id"),
                getDDLCreateIndex     ("archived_swimlane", "ix_arch_swimlane_process", "process_id"),
                getDDLCreateForeignKey("archived_swimlane", "fk_arch_swimlane_process", "process_id", "archived_process", "id"),
                getDDLCreateForeignKey("archived_swimlane", "fk_arch_swimlane_executor", "executor_id", "executor", "id"),

                getDDLCreatePrimaryKey("archived_variable", "pk_archived_variable", "id"),
                getDDLCreateIndex     ("archived_variable", "ix_arch_variable_name", "name"),
                getDDLCreateIndex     ("archived_variable", "ix_arch_variable_process", "process_id"),
                getDDLCreateForeignKey("archived_variable", "fk_arch_variable_process", "process_id", "archived_process", "id"),
                getDDLCreateUniqueKey ("archived_variable", "uk_arch_variable_process", "process_id", "name"),

                getDDLCreatePrimaryKey("archived_log", "pk_archived_log", "id"),
                getDDLCreateIndex("archived_log", "ix_arch_log_process", "process_id")
        );
    }
}
