package ru.runa.wfe.commons.dbmigration;

import ru.runa.wfe.commons.dbmigration.impl.CorrectChatRoomViewRenameColumn;

public class RecreateChatRoomView extends CorrectChatRoomViewRenameColumn {

    protected void executeDDLAfter() throws Exception {
        super.executeDDLAfter();
    }
}
