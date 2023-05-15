package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class DeleteBatchPresentationsRm3056 extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates("DELETE FROM BATCH_PRESENTATION WHERE CATEGORY = 'listTokenErrorsForm'");
    }

}
