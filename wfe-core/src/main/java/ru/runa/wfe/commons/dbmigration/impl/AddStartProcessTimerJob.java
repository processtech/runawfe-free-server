package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddStartProcessTimerJob extends DbMigration {

    @Override
    protected void executeDDLBefore() {

        executeUpdates(
                getDDLModifyColumnNullability("bpm_job", new BigintColumnDef("process_id")),
                getDDLCreateColumn("bpm_job", new VarcharColumnDef("timer_event_type", 8)),
                getDDLCreateColumn("bpm_job", new VarcharColumnDef("timer_event_expression", 50)),
                getDDLCreateColumn("bpm_job", new TimestampColumnDef("timer_event_next_date")),
                getDDLCreateColumn("bpm_job", new BigintColumnDef("timer_event_remaining_count")),
                getDDLCreateColumn("bpm_job", new BigintColumnDef("definition_version_id")),
                getDDLCreateIndex("bpm_job", "ix_job_definition_ver", "definition_version_id"),
                getDDLCreateForeignKey("bpm_job", "fk_job_definition_ver", "definition_version_id", "bpm_process_definition", "id")
        );
    }
}
