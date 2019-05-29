package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSettingsTable extends DbMigration {

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("BPM_SETTING", list(
                        new BigintColumnDef("ID").notNull().primaryKey(),
                        new VarcharColumnDef("FILE_NAME", 1024).notNull(),
                        new VarcharColumnDef("NAME", 1024).notNull(),
                        new VarcharColumnDef("VALUE", 1024).notNull()
                )),
                getDDLCreateSequence("SEQ_BPM_SETTING")
        );
    }
}
