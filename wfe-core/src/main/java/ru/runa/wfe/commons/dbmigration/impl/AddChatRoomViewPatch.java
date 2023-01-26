package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddChatRoomViewPatch extends DbMigration {
    @Override
    protected void executeDDLAfter() throws Exception {
        executeUpdates("CREATE view " + schemaPrefix + "V_CHAT_ROOM as SELECT distinct process.*, cmr.executor_id, " +
                "(SELECT count(*) from " + schemaPrefix + "CHAT_MESSAGE_RECIPIENT cr INNER JOIN " + schemaPrefix + "CHAT_MESSAGE cm ON cm.id = cr.message_id " +
                "WHERE cr.read_date IS NULL AND cm.process_id = process.id AND cr.executor_id = cmr.executor_id) as NEW_MESSAGES_COUNT " +
                "from " + schemaPrefix + "BPM_PROCESS process " +
                "inner join " + schemaPrefix + "CHAT_MESSAGE cm ON cm.process_id = process.id " +
                "inner join " + schemaPrefix + "CHAT_MESSAGE_RECIPIENT cmr ON cmr.message_id = cm.id ");
    }
}
