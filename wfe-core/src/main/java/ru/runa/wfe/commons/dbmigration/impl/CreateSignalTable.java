package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateSignalTable extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("BPM_SIGNAL", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new BigintColumnDef("VERSION").notNull(),
                        new TimestampColumnDef("CREATE_DATE").notNull(),
                        new TimestampColumnDef("EXPIRY_DATE"),
                        new BlobColumnDef("MESSAGE_SELECTORS_MAP").notNull(),
                        new BlobColumnDef("MESSAGE_DATA_MAP").notNull(),
                        new VarcharColumnDef("MESSAGE_SELECTORS", 1024),
                        new VarcharColumnDef("MESSAGE_DATA", 1024).notNull()
                )),
                getDDLCreateSequence("SEQ_BPM_SIGNAL"),
                getDDLCreateIndex("BPM_SIGNAL", "IX_MESSAGE_SELECTORS", "MESSAGE_SELECTORS")
        );
    }
    
}
