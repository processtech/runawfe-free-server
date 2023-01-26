package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Adds SYSTEM_LOG.PROCESS_LOG_CLEAN_BEFORE_DATE column
 * 
 * @since 4.4.3
 * @author vromav
 */
public class AddProcessLogCleanBeforeDateColumnPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("SYSTEM_LOG", new TimestampColumnDef("PROCESS_LOG_CLEAN_BEFORE_DATE")));
    }
}
