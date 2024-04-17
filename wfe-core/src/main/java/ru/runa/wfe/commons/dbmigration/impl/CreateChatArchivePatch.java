package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateChatArchivePatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("ARCHIVED_CHAT_MESSAGE", list(
                        new BigintColumnDef("ID").notNull().primaryKeyNoAutoInc(),
                        new TimestampColumnDef("CREATE_DATE").notNull(),
                        new BigintColumnDef("CREATE_ACTOR_ID").notNull(),
                        new BigintColumnDef("PROCESS_ID").notNull(),
                        new VarcharColumnDef("TEXT", 2048),
                        new ClobColumnDef("LONG_TEXT"))
                ),
                getDDLCreateTable("ARCHIVED_CHAT_MESSAGE_FILE", list(
                        new BigintColumnDef("ID").notNull().primaryKeyNoAutoInc(),
                        new BigintColumnDef("MESSAGE_ID"),
                        new VarcharColumnDef("FILE_NAME", 1024),
                        new VarcharColumnDef("UUID", 36).notNull())
                ),
                getDDLCreateTable("ARCHIVED_CHAT_MSG_RECIPIENT", list(
                        new BigintColumnDef("ID").notNull().primaryKeyNoAutoInc(),
                        new BigintColumnDef("MESSAGE_ID").notNull(),
                        new BigintColumnDef("ACTOR_ID").notNull(),
                        new TimestampColumnDef("READ_DATE"),
                        new BooleanColumnDef("MENTIONED").notNull(),
                        new VarcharColumnDef("FILE_NAME", 512)))
        );
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLCreateForeignKey("ARCHIVED_CHAT_MESSAGE", "FK_ARCH_CH_MSG_ACTOR_ID", "CREATE_ACTOR_ID", "EXECUTOR", "ID"),
                getDDLCreateIndex("ARCHIVED_CHAT_MESSAGE", "IX_ARCH_CH_M_PROCESS_ACTOR", "PROCESS_ID", "CREATE_ACTOR_ID"),
                getDDLCreateForeignKey("ARCHIVED_CHAT_MESSAGE", "FK_ARCH_CH_MSG_PROCESS_ID", "PROCESS_ID", "ARCHIVED_PROCESS", "ID"),
                getDDLCreateForeignKey("ARCHIVED_CHAT_MESSAGE_FILE", "FK_ARCH_CH_MSG_FILE_ID", "MESSAGE_ID", "ARCHIVED_CHAT_MESSAGE", "ID"),
                getDDLCreateIndex("ARCHIVED_CHAT_MESSAGE_FILE", "IX_ARCH_CH_MSG_FILE_MSG", "MESSAGE_ID"),
                getDDLCreateForeignKey("ARCHIVED_CHAT_MSG_RECIPIENT", "FK_ARCH_CH_MSG_RCPNT_M_ID", "MESSAGE_ID", "ARCHIVED_CHAT_MESSAGE", "ID"),
                getDDLCreateIndex("ARCHIVED_CHAT_MSG_RECIPIENT", "IX_ARCH_CH_MSG_RCPNT_M_ID", "MESSAGE_ID"),
                getDDLCreateForeignKey("ARCHIVED_CHAT_MSG_RECIPIENT", "FK_ARCH_CH_MSG_RPNT_A_ID", "ACTOR_ID", "EXECUTOR", "ID"),
                getDDLCreateIndex("ARCHIVED_CHAT_MSG_RECIPIENT", "IX_ARCH_CH_MSG_RCPNT_A_R", "ACTOR_ID", "READ_DATE")
        );
    }
}
