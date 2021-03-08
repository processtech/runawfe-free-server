package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @author Alekseev Mikhail
 * @since #1912
 */
public class RemoveWfeConstants extends DbMigration {
    @Override
    protected void executeDDLBefore() throws Exception {
        super.executeDDLBefore();
        executeUpdates(
                getDDLDropTable("wfe_constants"),
                getDDLDropSequence("seq_wfe_constants")
        );
    }
}
