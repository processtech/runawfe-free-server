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
        final String STORAGE_PATH = SystemProperties.getChatFileStoragePath();
        if (new File(STORAGE_PATH).mkdir()) {
            log.info("Created " + STORAGE_PATH);
        }
    }

    @Override
    protected void executeDDLBefore() {
        final String TABLE_NAME = "CHAT_MESSAGE_FILE";
        executeUpdates(
                getDDLCreateColumn(TABLE_NAME, new VarcharColumnDef("UUID", 36).notNull()),
                getDDLDropColumn(TABLE_NAME, "BYTES"));
    }
}
