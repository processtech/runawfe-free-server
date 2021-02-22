package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @since 02.02.2020
 * @author misharum
 */
public class CreateChatDbPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(//
                getDDLCreateTable("CHAT_MESSAGE", list(//
                        new BigintColumnDef("ID").notNull().primaryKey(), //
                        new TimestampColumnDef("CREATE_DATE").notNull(), //
                        new BigintColumnDef("CREATE_ACTOR_ID").notNull(), //
                        new BigintColumnDef("PROCESS_ID").notNull(), //
                        new VarcharColumnDef("TEXT", 1024).notNull()) //
                ), //
                getDDLCreateSequence("SEQ_CHAT_MESSAGE"), //
                getDDLCreateTable("CHAT_MESSAGE_FILE", list(//
                        new BigintColumnDef("ID").notNull().primaryKey(), //
                        new BigintColumnDef("MESSAGE_ID"), //
                        new VarcharColumnDef("FILE_NAME", 1024), //
                        new BlobColumnDef("BYTES")) //
                ),//
                getDDLCreateSequence("SEQ_CHAT_MESSAGE_FILE"), //
                getDDLCreateTable("CHAT_MESSAGE_RECIPIENT", list(//
                        new BigintColumnDef("ID").notNull().primaryKey(), //
                        new BigintColumnDef("MESSAGE_ID").notNull(), //
                        new BigintColumnDef("EXECUTOR_ID").notNull(),//
                        new TimestampColumnDef("READ_DATE"), //
                        new BooleanColumnDef("MENTIONED").notNull(), //
                        new VarcharColumnDef("FILE_NAME", 512))), //
                getDDLCreateSequence("SEQ_CHAT_MESSAGE_RECIPIENT")//
        );
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(//
                getDDLCreateForeignKey("CHAT_MESSAGE", "FK_CHAT_MESSAGE_EXECUTOR_ID", "CREATE_ACTOR_ID", "EXECUTOR", "ID"),//
                getDDLCreateIndex("CHAT_MESSAGE", "IX_CHAT_MESSAGE_PROCESS_ACTOR", "PROCESS_ID", "CREATE_ACTOR_ID"),//
                getDDLCreateForeignKey("CHAT_MESSAGE", "FK_CHAT_MESSAGE_PROCESS_ID", "PROCESS_ID", "BPM_PROCESS", "ID"),//
                getDDLCreateForeignKey("CHAT_MESSAGE_FILE", "FK_CHAT_MESSAGE_FILE_ID", "MESSAGE_ID", "CHAT_MESSAGE", "ID"), //
                getDDLCreateIndex("CHAT_MESSAGE_FILE", "IX_CHAT_MESSAGE_FILE_MESSAGE", "MESSAGE_ID"), //
                getDDLCreateForeignKey("CHAT_MESSAGE_RECIPIENT", "FK_CHAT_MESSAGE_RECIPIENT_M_ID", "MESSAGE_ID", "CHAT_MESSAGE", "ID"), //
                getDDLCreateIndex("CHAT_MESSAGE_RECIPIENT", "IX_CHAT_MESSAGE_RECIPIENT_M_ID", "MESSAGE_ID"), //
                getDDLCreateForeignKey("CHAT_MESSAGE_RECIPIENT", "FK_CHAT_MESSAGE_RECIPIENT_E_ID", "EXECUTOR_ID", "EXECUTOR", "ID"), //
                getDDLCreateIndex("CHAT_MESSAGE_RECIPIENT", "IX_CHAT_MESSAGE_RECIPIENT_E_R", "EXECUTOR_ID", "READ_DATE")//
        );
    }

}
