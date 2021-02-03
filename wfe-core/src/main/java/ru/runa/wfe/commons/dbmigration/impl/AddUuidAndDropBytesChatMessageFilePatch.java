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
    private final String ID = "ID";
    private final String UUID = "UUID";
    private final String BYTES = "BYTES";

    private final String STORAGE_PATH;

    @Autowired
    private ChatFileIo chatFileIo;

    public AddUuidAndDropBytesChatMessageFilePatch() {
        STORAGE_PATH = SystemProperties.getChatFileStoragePath();
        new File(STORAGE_PATH).mkdir();
    }

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn(TABLE_NAME, new VarcharColumnDef(UUID, 36)));
    }

    @Override
    public void executeDML(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM " + TABLE_NAME;
        ResultSet resultSet = statement.executeQuery(sql);
        List<ChatMessageFile> files = new ArrayList<>();
        try {
            while (resultSet.next()) {
                long id = resultSet.getLong(ID);
                byte[] bytes = resultSet.getBytes(BYTES);
                files.add(chatFileIo.save(new ChatMessageFileDto(id, "", bytes)));
            }
            List<String> queries = new ArrayList<>(files.size());
            for (ChatMessageFile file : files) {
                queries.add("UPDATE " + TABLE_NAME + " SET " + UUID + " = '" + file.getUuid() + "' WHERE " + ID + " = " + file.getId());
            }
            executeUpdates(queries);
            log.info("All files saved to directory: " + STORAGE_PATH);
        } catch (Exception exception) {
            executeUpdates(getDDLDropColumn(TABLE_NAME, UUID));
            chatFileIo.delete(files);
            log.error("Files not saved: ", exception);
            throw exception;
        }
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLDropColumn(TABLE_NAME, BYTES),
                getDDLModifyColumnNullability(TABLE_NAME, new VarcharColumnDef(UUID, 36).notNull()));
    }
}
