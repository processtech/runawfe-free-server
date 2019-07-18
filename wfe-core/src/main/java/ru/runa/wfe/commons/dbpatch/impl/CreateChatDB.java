package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import ru.runa.wfe.commons.dbpatch.DbPatch;

public class CreateChatDB extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        List<ColumnDef> columns = new LinkedList<DbPatch.ColumnDef>();
        ColumnDef id = new ColumnDef("MESSAGE_ID", Types.BIGINT, false);
        id.setPrimaryKey();

        columns.add(id);
        columns.add(new ColumnDef("TEXT", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("USER_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("IERARCHY_MESSAGE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("CHAT_ID", Types.INTEGER, false));
        columns.add(new ColumnDef("USER_ID", Types.BIGINT, false));
        columns.add(new ColumnDef("Message_Date", Types.TIMESTAMP, false));

        sql.add(getDDLCreateTable("CHAT_MESSAGE", columns, null));
        sql.add(getDDLCreateSequence("SEQ_CHAT_MESSAGE"));
        // user-DB
        List<ColumnDef> columns2 = new LinkedList<DbPatch.ColumnDef>();
        ColumnDef id2_1 = new ColumnDef("USER_ID", Types.BIGINT, false);
        ColumnDef id2_2 = new ColumnDef("USER_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false);
        ColumnDef id2_3 = new ColumnDef("CHAT_ID", Types.INTEGER, false);
        ColumnDef id2_4 = new ColumnDef("ID", Types.BIGINT, false);
        id2_4.setPrimaryKey();

        columns2.add(id2_1);
        columns2.add(id2_2);
        columns2.add(id2_3);
        columns2.add(id2_4);
        columns2.add(new ColumnDef("LAST_MESSAGE_NAME", Types.BIGINT, false));

        sql.add(getDDLCreateTable("CHATS_USER_INFO", columns2, null));
        sql.add(getDDLCreateSequence("SEQ_CHAT_USER_INFO"));
        return sql;
    }

    public void test() {
        getDDLQueriesBefore();
    }

}
