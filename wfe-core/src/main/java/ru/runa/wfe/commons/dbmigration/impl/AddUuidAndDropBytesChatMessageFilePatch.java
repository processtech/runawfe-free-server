package ru.runa.wfe.commons.dbmigration.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileIo;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @author Sergey Inyakin
 */
public class AddUuidAndDropBytesChatMessageFilePatch extends DbMigration {
    private final String TABLE_NAME = "CHAT_MESSAGE_FILE";
    private final String STORAGE_PATH;

    public AddUuidAndDropBytesChatMessageFilePatch() {
        STORAGE_PATH = SystemProperties.getChatFileStoragePath();
        new File(STORAGE_PATH).mkdir();
    }

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn(TABLE_NAME, new VarcharColumnDef("UUID", 36).notNull()),
                getDDLDropColumn(TABLE_NAME, "BYTES"));
    }
}
