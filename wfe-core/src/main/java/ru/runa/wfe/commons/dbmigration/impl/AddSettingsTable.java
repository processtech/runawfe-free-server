package ru.runa.wfe.commons.dbmigration.impl;

import java.util.List;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSettingsTable extends DbMigration {

    @Override
    @SuppressWarnings("ConstantConditions")
    protected List<String> getDDLQueriesBefore() {
        return list(
                getDDLCreateTable("BPM_SETTING", list(
                        new BigintColumnDef("ID", false).setPrimaryKey(),
                        new VarcharColumnDef("FILE_NAME", 1024, false),
                        new VarcharColumnDef("NAME", 1024, false),
                        new VarcharColumnDef("VALUE", 1024, true)
                )),
                getDDLCreateSequence("SEQ_BPM_SETTING")
        );
    }
}
