package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateChatDbPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(//
                getDDLCreateTable("chat_message", list(//
                        new BigintColumnDef("id").notNull().primaryKey(), //
                        new TimestampColumnDef("create_date").notNull(), //
                        new BigintColumnDef("create_actor_id").notNull(), //
                        new BigintColumnDef("process_id").notNull(), //
                        new VarcharColumnDef("text", 1024).notNull(), //
                        new VarcharColumnDef("quoted_message_ids", 1024)) //
                ), //
                getDDLCreateSequence("seq_chat_message"), //
                getDDLCreateTable("chat_message_file", list(//
                        new BigintColumnDef("id").notNull().primaryKey(), //
                        new BigintColumnDef("message_id"), //
                        new VarcharColumnDef("file_name", 1024), //
                        new BlobColumnDef("bytes")) //
                ),//
                getDDLCreateSequence("seq_chat_message_file"), //
                getDDLCreateTable("chat_message_recipient", list(//
                        new BigintColumnDef("id").notNull().primaryKey(), //
                        new BigintColumnDef("message_id").notNull(), //
                        new BigintColumnDef("executor_id").notNull(),//
                        new TimestampColumnDef("read_date"), //
                        new BooleanColumnDef("mentioned").notNull(), //
                        new VarcharColumnDef("file_name", 512))), //
                getDDLCreateSequence("seq_chat_message_recipient")//
        );
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(//
                getDDLCreateForeignKey("chat_message", "fk_chat_message_executor_id", "create_actor_id", "executor", "id"),//
                getDDLCreateIndex("chat_message", "ix_chat_message_process_actor", "process_id", "create_actor_id"),//
                getDDLCreateForeignKey("chat_message", "fk_chat_message_process_id", "process_id", "bpm_process", "id"),//
                getDDLCreateForeignKey("chat_message_file", "fk_chat_message_file_id", "message_id", "chat_message", "id"), //
                getDDLCreateIndex("chat_message_file", "ix_chat_message_file_message", "message_id"), //
                getDDLCreateForeignKey("chat_message_recipient", "fk_chat_message_recipient_m_id", "message_id", "chat_message", "id"), //
                getDDLCreateIndex("chat_message_recipient", "ix_chat_message_recipient_m_id", "message_id"), //
                getDDLCreateForeignKey("chat_message_recipient", "fk_chat_message_recipient_e_id", "executor_id", "executor", "id"), //
                getDDLCreateIndex("chat_message_recipient", "ix_chat_message_recipient_e_r", "executor_id", "read_date")//
        );
    }

}
