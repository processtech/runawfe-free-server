package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class RenameColumnInChatMessageRecipientPatch extends DbMigration {
    @Override
    protected void executeDDLBefore() throws Exception {
        executeUpdates(getDDLRenameColumn("CHAT_MESSAGE_RECIPIENT","EXECUTOR_ID", new BigintColumnDef("ACTOR_ID")));
    }
}
