package ru.runa.wfe.commons.dbmigration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @author Sergey Inyakin
 */
public class AddUuidAndDropBytesChatMessageFilePatch extends DbMigration {
    private static final String TABLE_NAME = "CHAT_MESSAGE_FILE";

    private String storagePath;

    public AddUuidAndDropBytesChatMessageFilePatch() {
        storagePath = SystemProperties.getChatFileStoragePath();
        Path storageDir = Paths.get(storagePath);
        try {
            Files.createDirectory(storageDir);
        } catch (IOException ignored) {}
    }

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn(TABLE_NAME, new VarcharColumnDef("UUID", 36)));
    }

    @Override
    public void executeDML(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM " + TABLE_NAME;
        Map<Long, String> map = new HashMap<>();
        ResultSet resultSet = statement.executeQuery(sql);
        try {
            while (resultSet.next()) {
                long id = resultSet.getLong("ID");
                InputStream inputStream = resultSet.getBinaryStream("BYTES");
                writeFile(inputStream, id, map);
            }
            List<String> queries = new ArrayList<>();
            for (Map.Entry<Long, String> entry : map.entrySet())
                queries.add("UPDATE " + TABLE_NAME + " SET UUID = '" + entry.getValue() + "' WHERE id = " + entry.getKey());
            executeUpdates(queries);
        } catch (Exception exception) {
            deleteFiles(map);
            throw exception;
        }
    }

    private void writeFile(InputStream inputStream, long id, Map<Long, String> map) throws IOException {
        String uuidName;
        Path path;
        do {
            uuidName = UUID.randomUUID().toString();
            path = Paths.get(storagePath + "/" + uuidName);
        } while (Files.isRegularFile(path));
        map.put(id, uuidName);
        Files.copy(inputStream, path);
    }

    private void deleteFiles(Map<Long, String> map) {
        for (Map.Entry<Long, String> entry : map.entrySet()) {
            Path path = Paths.get(storagePath + "/" + entry.getValue());
            try {
                Files.delete(path);
            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLModifyColumnNullability(TABLE_NAME, new BlobColumnDef("BYTES")),
                getDDLModifyColumnNullability(TABLE_NAME, new VarcharColumnDef("UUID", 36).notNull()));
    }
}
