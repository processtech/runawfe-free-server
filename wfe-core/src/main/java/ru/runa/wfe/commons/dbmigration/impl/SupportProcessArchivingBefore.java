package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.Statement;
import lombok.val;
import lombok.var;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * See TNMS #5006.
 * <p>
 * "Not null" constraints, indexes and foreign keys for ARCHIVED_* tables are copy-pasted from current BPM_* tables,
 * except those which may result in FK violation during batch insert-select in ProcessArchiver.
 */
public class SupportProcessArchivingBefore extends DbMigration {

    @Override
    protected void executeDDLBefore() throws Exception {

        // Sanity check.
        val conn = sessionFactory.getCurrentSession().connection();
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("select count(*) from bpm_agglog_tasks where end_reason is not null and end_reason not between -1 and 6");
            rs.next();
            long n = rs.getLong(1);
            if (n > 0) {
                throw new RuntimeException("Column bpm_agglog_tasks.end_reason contains " + n + " invalid value(s). Migration aborted.");
            }

            rs = stmt.executeQuery("select count(*) from bpm_agglog_assignments where discriminator <> 'T'");
            rs.next();
            n = rs.getLong(1);
            if (n > 0) {
                throw new RuntimeException("Column bpm_agglog_assignments.discriminator contains " + n + " non-'T' values. Migration aborted.");
            }

            rs = stmt.executeQuery("select count(*) from bpm_agglog_assignments where assignment_object_id is null");
            rs.next();
            n = rs.getLong(1);
            if (n > 0) {
                throw new RuntimeException("Column bpm_agglog_assignments.assignment_object_id contains " + n + " null(s). Migration aborted.");
            }
        }

        executeUpdates(
                getDDLDropTable("bpm_agglog_process"),

                getDDLRenameTable("bpm_agglog_tasks", "bpm_agglog_task"),
                getDDLRenameSequence("seq_bpm_agglog_tasks", "seq_bpm_agglog_task"),
                getDDLRenameIndex("bpm_agglog_task", "ix_agglog_tasks_create_date", "ix_agglog_task_create_date"),
                getDDLRenameIndex("bpm_agglog_task", "ix_agglog_tasks_end_date", "ix_agglog_task_end_date"),
                getDDLRenameIndex("bpm_agglog_task", "ix_agglog_tasks_process", "ix_agglog_task_process"),
                getDDLRenameColumn("bpm_agglog_task", "end_reason", new IntColumnDef("end_reason_old")),
                getDDLCreateColumn("bpm_agglog_task", new VarcharColumnDef("end_reason", 16)),

                getDDLRenameTable("bpm_agglog_assignments", "bpm_agglog_assignment"),
                getDDLRenameSequence("seq_bpm_agglog_assignments", "seq_bpm_agglog_assignment"),
                getDDLDropColumn("bpm_agglog_assignment", "discriminator"),
                getDDLDropColumn("bpm_agglog_assignment", "idx"),
                getDDLRenameColumn("bpm_agglog_assignment", "assignment_object_id", new BigintColumnDef("agglog_task_id")),
                getDDLModifyColumnNullability("bpm_agglog_assignment", new BigintColumnDef("agglog_task_id").notNull())
        );
    }

    @Override
    public void executeDML(Connection conn) {
        executeUpdates(
                "update bpm_agglog_task set end_reason = case end_reason_old " +
                        "when -1 then 'UNKNOWN' " +
                        "when 0 then 'PROCESSING' " +
                        "when 1 then 'COMPLETED' " +
                        "when 2 then 'CANCELLED' " +
                        "when 3 then 'TIMEOUT' " +
                        "when 4 then 'SUBSTITUTOR_END' " +
                        "when 5 then 'PROCESS_END' " +
                        "when 6 then 'ADMIN_END' " +
                        "else null " +
                        "end"
        );
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLDropColumn("bpm_agglog_task", "end_reason_old")
        );
    }
}
