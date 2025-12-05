package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateTaskFormDraftPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() throws Exception {
        executeUpdates(
                getDDLCreateSequence("SEQ_BPM_TASK_FORM_DRAFT"),

                getDDLCreateTable("BPM_TASK_FORM_DRAFT", list(
                        new BigintColumnDef("ID").primaryKeyNoAutoInc(),
                        new BigintColumnDef("TASK_ID").notNull(),
                        new BigintColumnDef("ACTOR_ID").notNull(),
                        new BigintColumnDef("VERSION").notNull(),
                        new ClobColumnDef("DATA_B64"))
                )
        );
    }

    @Override
    protected void executeDDLAfter() throws Exception {
        executeUpdates(
                getDDLCreateUniqueKey("BPM_TASK_FORM_DRAFT", "UK_BPM_TASK_FORM_DRAFT_T_A", "TASK_ID", "ACTOR_ID")
        );
    }
}
