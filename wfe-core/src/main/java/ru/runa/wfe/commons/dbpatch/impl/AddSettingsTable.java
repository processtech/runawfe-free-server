package ru.runa.wfe.commons.dbpatch.impl;

import com.google.common.collect.ImmutableList;
import java.util.List;
import ru.runa.wfe.commons.dbpatch.DbPatch;

public class AddSettingsTable extends DbPatch {

    @Override
    @SuppressWarnings("ConstantConditions")
    protected List<String> getDDLQueriesBefore() {
        return ImmutableList.of(
                getDDLCreateTable("BPM_SETTING", ImmutableList.of(
                        new BigintColumnDef("ID", false).setPrimaryKey(),
                        new VarcharColumnDef("FILE_NAME", 1024, false),
                        new VarcharColumnDef("NAME", 1024, false),
                        new VarcharColumnDef("VALUE", 1024, true)
                )),
                getDDLCreateSequence("SEQ_BPM_SETTING")
        );
    }
}
