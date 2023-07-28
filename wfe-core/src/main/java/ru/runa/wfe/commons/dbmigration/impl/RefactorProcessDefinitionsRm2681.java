package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class RefactorProcessDefinitionsRm2681 extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLRenameTable("bpm_process_definition", "bpm_process_definition_pack"),
                getDDLRenameTable("bpm_process_definition_ver", "bpm_process_definition"),
                getDDLRenameColumn("archived_process", "definition_version_id", new BigintColumnDef("definition_id")),
                getDDLRenameColumn("bpm_process", "definition_version_id", new BigintColumnDef("definition_id")),
                getDDLRenameColumn("bpm_process_definition_pack", "latest_version_id", new BigintColumnDef("latest_definition_id")),
                getDDLRenameColumn("bpm_process_definition", "definition_id", new BigintColumnDef("pack_id")),
                getDDLRenameSequence("seq_bpm_process_definition", "seq_bpm_process_definition_pc"),
                getDDLRenameSequence("seq_bpm_process_definition_ver", "seq_bpm_process_definition")
        );
    }
}
