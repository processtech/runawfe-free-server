package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddAsyncForTaskAndSubprocess extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_TASK", new BooleanColumnDef("ASYNC")));
        executeUpdates(getDDLCreateColumn("BPM_SUBPROCESS", new BooleanColumnDef("ASYNC")));
    }

    @Override
    public void executeDML(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            switch (dbType) {
                case POSTGRESQL: {
                    stmt.executeUpdate("" +
                            "UPDATE BPM_TASK T SET ASYNC = CASE\n" +
                            "    WHEN EXISTS(SELECT IT.ID FROM BPM_TASK IT INNER JOIN BPM_TOKEN TK ON IT.TOKEN_ID = TK.ID AND IT.NODE_ID <> TK.NODE_ID WHERE IT.ID = T.ID)\n" +
                            "    THEN TRUE ELSE FALSE END");
                    stmt.executeUpdate("" +
                            "UPDATE BPM_SUBPROCESS S SET ASYNC = CASE\n" +
                            "    WHEN EXISTS(SELECT SP.ID FROM BPM_SUBPROCESS SP INNER JOIN BPM_TOKEN TK ON SP.PARENT_TOKEN_ID = TK.ID AND SP.PARENT_NODE_ID <> TK.NODE_ID WHERE SP.ID = S.ID)\n" +
                            "    THEN TRUE ELSE FALSE END\n" +
                            "    WHERE PARENT_PROCESS_ID IN (SELECT ID FROM BPM_PROCESS WHERE END_DATE IS NULL)");
                    break;
                }
                default: {
                    stmt.executeUpdate("" +
                            "UPDATE BPM_TASK T SET T.ASYNC = CASE\n" +
                            "    WHEN EXISTS(SELECT IT.ID FROM BPM_TASK IT INNER JOIN BPM_TOKEN TK ON IT.TOKEN_ID = TK.ID AND IT.NODE_ID <> TK.NODE_ID WHERE IT.ID = T.ID)\n" +
                            "    THEN 1 ELSE 0 END");
                    stmt.executeUpdate("" +
                            "UPDATE BPM_SUBPROCESS S SET S.ASYNC = CASE\n" +
                            "    WHEN EXISTS(SELECT SP.ID FROM BPM_SUBPROCESS SP INNER JOIN BPM_TOKEN TK ON SP.PARENT_TOKEN_ID = TK.ID AND SP.PARENT_NODE_ID <> TK.NODE_ID WHERE SP.ID = S.ID)\n" +
                            "    THEN 1 ELSE 0 END\n" +
                            "    WHERE PARENT_PROCESS_ID IN (SELECT ID FROM BPM_PROCESS WHERE END_DATE IS NULL)");
                }
            }
        }
    }
}
