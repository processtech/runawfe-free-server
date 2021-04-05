package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddChatRoomViewPatch extends DbMigration {
    @Override
    protected void executeDDLAfter() throws Exception {
        executeUpdates("CREATE view CHAT_ROOM as SELECT distinct process.*, cmr.executor_id, " +
                "(SELECT count(*) from CHAT_MESSAGE_RECIPIENT cr INNER JOIN CHAT_MESSAGE cm ON cm.id = cr.message_id " +
                "WHERE cr.read_date IS NULL AND cm.process_id = process.id AND cr.executor_id = cmr.executor_id) as NEW_MESSAGES_COUNT " +
                "from BPM_PROCESS process " +
                "inner join CHAT_MESSAGE cm ON cm.process_id = process.id " +
                "inner join CHAT_MESSAGE_RECIPIENT cmr ON cmr.message_id = cm.id ");
    }
}
