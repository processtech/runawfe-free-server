package ru.runa.wfe.commons.dbmigration.impl;

import java.io.File;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @author Sergey Inyakin
 */
@CommonsLog
public class AddUuidAndDropBytesChatMessageFilePatch extends DbMigration {

    public AddUuidAndDropBytesChatMessageFilePatch() {
        final String chatFilesStoragePath = SystemProperties.getChatFileStoragePath();
        if (new File(chatFilesStoragePath).mkdir()) {
            log.info("Created " + chatFilesStoragePath);
        }
    }

    @Override
    protected void executeDDLBefore() {
        final String chatMessageFileTableName = "CHAT_MESSAGE_FILE";
        executeUpdates(
                getDDLCreateColumn(chatMessageFileTableName, new VarcharColumnDef("UUID", 36).notNull()),
                getDDLDropColumn(chatMessageFileTableName, "BYTES"));
    }
}
