package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * See TNMS #5006.
 * <p>
 * "Not null" constraints, indexes and foreign keys for ARCHIVED_* tables are copy-pasted from current BPM_* tables,
 * except those which may result in FK violation during batch insert-select in ProcessArchiver.
 */
public class SupportProcessArchiving extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        val sqls = new ArrayList<String>(Arrays.asList(
                // Nullable per-definition configuration; default is SystemProperties.getProcessDefaultSecondsBeforeArchiving():
                getDDLCreateColumn("bpm_process_definition", new IntColumnDef("seconds_before_archiving", true)),

                // Process: all fields except EXECUTION_STATUS.
                "create table archived_process as " +
                        "select id, parent_id, tree_path, start_date, end_date, version, definition_version_id, root_token_id " +
                        "from bpm_process " +
                        "where 0=1",
                getDDLCreatePrimaryKey("archived_process", "pk_archived_process", "id"),
                getDDLCreateIndex     ("archived_process", "ix_arch_process_def_ver", "definition_version_id"),
                getDDLCreateForeignKey("archived_process", "fk_arch_process_def_ver", "definition_version_id", "bpm_process_definition_ver", "id"),
                getDDLCreateIndex     ("archived_process", "ix_arch_process_root_token", "root_token_id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_process", "fk_arch_process_root_token", "root_token_id", "archived_token", "id")

                // Token: all fields except EXECUTION_STATUS:
                "create table archived_token as " +
                        "select id, error_message, transition_id, message_selector, start_date, end_date, error_date, node_id, " +
                        "       reactivate_parent, node_type, version, name, process_id, parent_id " +
                        "from bpm_token " +
                        "where 0=1",
                getDDLCreatePrimaryKey("archived_token", "pk_archived_token", "id"),
                getDDLCreateIndex     ("archived_token", "ix_arch_message_selector", "message_selector"),
                getDDLCreateIndex     ("archived_token", "ix_arch_token_parent", "parent_id"),
                getDDLCreateIndex     ("archived_token", "ix_arch_token_process", "process_id"),
                getDDLCreateForeignKey("archived_token", "fk_arch_token_process", "process_id", "archived_process", "id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_token", "fk_arch_token_parent", "parent_id", "archived_token", "id")

                // NodeProcess: all fields.
                "create table archived_subprocess as " +
                        "select * " +
                        "from bpm_subprocess " +
                        "where 0=1",
                getDDLCreatePrimaryKey("archived_subprocess", "pk_archived_subprocess", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_process", "process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_process", "process_id", "archived_process", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_parent", "parent_process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_parent", "parent_process_id", "archived_process", "id"),
                getDDLCreateIndex     ("archived_subprocess", "ix_arch_subprocess_root", "root_process_id"),
                getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_root", "root_process_id", "archived_process", "id"),
                // Not created: would be violated during batch insert-select in ProcessArchiver.
                //getDDLCreateForeignKey("archived_subprocess", "fk_arch_subprocess_token", "parent_token_id", "archived_token", "id")

                // Swimlane: all fields.
                "create table archived_swimlane as " +
                        "select * " +
                        "from bpm_swimlane " +
                        "where 0=1",
                getDDLCreatePrimaryKey("archived_swimlane", "pk_arch_swimlane", "id"),
                getDDLCreateIndex     ("archived_swimlane", "ix_arch_swimlane_process", "process_id"),
                getDDLCreateForeignKey("archived_swimlane", "fk_arch_swimlane_process", "process_id", "archived_process", "id"),
                getDDLCreateForeignKey("archived_swimlane", "fk_arch_swimlane_executor", "executor_id", "executor", "id"),

                // Variable: all fields.
                "create table archived_variable as " +
                        "select * " +
                        "from bpm_variable " +
                        "where 0=1",
                getDDLCreatePrimaryKey("archived_variable", "pk_archived_variable", "id"),
                getDDLCreateIndex     ("archived_variable", "ix_arch_variable_name", "name"),
                getDDLCreateIndex     ("archived_variable", "ix_arch_variable_process", "process_id"),
                getDDLCreateForeignKey("archived_variable", "fk_arch_variable_process", "process_id", "archived_process", "id"),
                getDDLCreateUniqueKey ("archived_variable", "uk_arch_variable_process", "process_id", "name"),

                // ProcessLog: all fields.
                "create table archived_log as " +
                        "select * " +
                        "from bpm_log " +
                        "where 0=1",
                getDDLCreatePrimaryKey("archived_log", "pk_archived_log", "id"),
                getDDLCreateIndex("archived_log", "ix_arch_log_process", "process_id")
        ));

        // ORA-01442: column to be modified to NOT NULL is already NOT NULL
        if (dbType != DbType.ORACLE) {
            sqls.addAll(Arrays.asList(
                    getDDLModifyColumnNullability("archived_process", "id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_process", "definition_version_id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_process", "root_token_id", dialect.getTypeName(Types.BIGINT), false),

                    getDDLModifyColumnNullability("archived_token", "id", dialect.getTypeName(Types.BIGINT), false),

                    getDDLModifyColumnNullability("archived_subprocess", "id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_subprocess", "create_date", dialect.getTypeName(Types.DATE), false),
                    getDDLModifyColumnNullability("archived_subprocess", "process_id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_subprocess", "parent_process_id", dialect.getTypeName(Types.BIGINT), false),

                    getDDLModifyColumnNullability("archived_swimlane", "id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_swimlane", "create_date", dialect.getTypeName(Types.DATE), false),

                    getDDLModifyColumnNullability("archived_variable", "id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_variable", "discriminator", dialect.getTypeName(Types.CHAR, 1, 1, 1), false),
                    getDDLModifyColumnNullability("archived_variable", "process_id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_variable", "create_date", dialect.getTypeName(Types.DATE), false),

                    getDDLModifyColumnNullability("archived_log", "discriminator", dialect.getTypeName(Types.CHAR, 1, 1, 1), false),
                    getDDLModifyColumnNullability("archived_log", "id", dialect.getTypeName(Types.BIGINT), false),
                    getDDLModifyColumnNullability("archived_log", "severity", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false),
                    getDDLModifyColumnNullability("archived_log", "create_date", dialect.getTypeName(Types.DATE), false),
                    getDDLModifyColumnNullability("archived_log", "process_id", dialect.getTypeName(Types.BIGINT), false)
            ));
        }

        return sqls;
    }
}
