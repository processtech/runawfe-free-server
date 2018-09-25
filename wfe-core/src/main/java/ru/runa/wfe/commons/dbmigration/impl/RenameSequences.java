package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Make sequence names match table names. This is critical for DbMigration0 code generator.
 */
public class RenameSequences extends DbMigration {

    @Override
    public void executeDDLBefore() {
        executeUpdates(
                getDDLRenameSequence("SEQ_EXECUTOR_RELATION", "SEQ_EXECUTOR_RELATION_PAIR"),
                getDDLRenameSequence("SEQ_RELATION_GROUP", "SEQ_EXECUTOR_RELATION")
        );
    }
}
