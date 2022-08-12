package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CorrectChatRoomViewRenameColumn extends DbMigration {

    @Override
    protected void executeDDLAfter() throws Exception {
        executeUpdates("DROP view V_CHAT_ROOM");
        executeUpdates("CREATE VIEW V_CHAT_ROOM AS " +
                "SELECT DISTINCT process.*, cmr.actor_id, " +
                    "(SELECT COUNT(*) FROM CHAT_MESSAGE_RECIPIENT cr " +
                    "INNER JOIN CHAT_MESSAGE cm ON cm.id = cr.message_id " +
                    "WHERE cr.read_date IS NULL AND cm.process_id = process.id AND cr.actor_id = cmr.actor_id) " +
                    "AS NEW_MESSAGES_COUNT " +
                "FROM BPM_PROCESS process " +
                "INNER JOIN CHAT_MESSAGE cm ON cm.process_id = process.id " +
                "INNER JOIN CHAT_MESSAGE_RECIPIENT cmr ON cmr.message_id = cm.id ");
    }
}
