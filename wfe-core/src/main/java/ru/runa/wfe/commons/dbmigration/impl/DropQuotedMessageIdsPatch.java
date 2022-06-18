package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class DropQuotedMessageIdsPatch extends DbMigration {
    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLDropColumn("CHAT_MESSAGE", "QUOTED_MESSAGE_IDS"));
    }
}
