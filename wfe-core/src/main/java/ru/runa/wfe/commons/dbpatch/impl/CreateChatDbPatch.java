package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * @since 02.02.2020
 * @author misharum
 */
public class CreateChatDbPatch extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        {
            List<ColumnDef> columns = new ArrayList<>();
            columns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
            columns.add(new ColumnDef("CREATE_DATE", Types.DATE, false));
            columns.add(new ColumnDef("CREATE_ACTOR_ID", Types.BIGINT, false));
            columns.add(new ColumnDef("PROCESS_ID", Types.BIGINT, false));
            columns.add(new ColumnDef("TEXT", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
            columns.add(new ColumnDef("QUOTED_MESSAGE_IDS", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
            sql.add(getDDLCreateTable("CHAT_MESSAGE", columns, null));
            sql.add(getDDLCreateSequence("SEQ_CHAT_MESSAGE"));
        }
        {
            List<ColumnDef> columns = new ArrayList<>();
            columns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
            columns.add(new ColumnDef("MESSAGE_ID", Types.BIGINT, true));
            columns.add(new ColumnDef("FILE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
            columns.add(new ColumnDef("BYTES", Types.BLOB, false));
            sql.add(getDDLCreateTable("CHAT_MESSAGE_FILE", columns, null));
            sql.add(getDDLCreateSequence("SEQ_CHAT_MESSAGE_FILE"));
        }
        {
            List<ColumnDef> columns = new ArrayList<>();
            columns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
            columns.add(new ColumnDef("MESSAGE_ID", Types.BIGINT, false));
            columns.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
            columns.add(new ColumnDef("READ_DATE", Types.DATE, true));
            columns.add(new ColumnDef("MENTIONED", Types.BIT, false));
            sql.add(getDDLCreateTable("CHAT_MESSAGE_RECIPIENT", columns, null));
            sql.add(getDDLCreateSequence("SEQ_CHAT_MESSAGE_RECIPIENT"));
        }
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateForeignKey("CHAT_MESSAGE", "FK_CHAT_MESSAGE_EXECUTOR_ID", "CREATE_ACTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("CHAT_MESSAGE", "IX_CHAT_MESSAGE_PROCESS_ACTOR", "PROCESS_ID", "CREATE_ACTOR_ID"));
        sql.add(getDDLCreateForeignKey("CHAT_MESSAGE", "FK_CHAT_MESSAGE_PROCESS_ID", "PROCESS_ID", "BPM_PROCESS", "ID"));
        sql.add(getDDLCreateForeignKey("CHAT_MESSAGE_FILE", "FK_CHAT_MESSAGE_FILE_ID", "MESSAGE_ID", "CHAT_MESSAGE", "ID"));
        sql.add(getDDLCreateIndex("CHAT_MESSAGE_FILE", "IX_CHAT_MESSAGE_FILE_MESSAGE", "MESSAGE_ID"));
        sql.add(getDDLCreateForeignKey("CHAT_MESSAGE_RECIPIENT", "FK_CHAT_MESSAGE_RECIPIENT_M_ID", "MESSAGE_ID", "CHAT_MESSAGE", "ID"));
        sql.add(getDDLCreateIndex("CHAT_MESSAGE_RECIPIENT", "IX_CHAT_MESSAGE_RECIPIENT_M_ID", "MESSAGE_ID"));
        sql.add(getDDLCreateForeignKey("CHAT_MESSAGE_RECIPIENT", "FK_CHAT_MESSAGE_RECIPIENT_E_ID", "EXECUTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("CHAT_MESSAGE_RECIPIENT", "IX_CHAT_MESSAGE_RECIPIENT_E_R", "EXECUTOR_ID", "READ_DATE"));
        return sql;
    }
}