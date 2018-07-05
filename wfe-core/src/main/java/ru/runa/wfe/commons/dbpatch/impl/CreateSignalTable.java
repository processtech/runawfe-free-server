package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class CreateSignalTable extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = new ArrayList<>();
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new BigintColumnDef("ID", false).setPrimaryKey());
        columns.add(new BigintColumnDef("VERSION", false));
        columns.add(new DateColumnDef("CREATE_DATE", false));
        columns.add(new DateColumnDef("DELETE_DATE", true));
        columns.add(new ColumnDef("MESSAGE_SELECTORS_MAP", dialect.getTypeName(Types.BLOB), false));
        columns.add(new ColumnDef("MESSAGE_DATA_MAP", dialect.getTypeName(Types.BLOB), false));
        columns.add(new VarcharColumnDef("MESSAGE_SELECTORS", 1024, true));
        columns.add(new VarcharColumnDef("MESSAGE_DATA", 1024, false));
        sql.add(getDDLCreateTable("BPM_SIGNAL", columns, null));
        sql.add(getDDLCreateSequence("SEQ_BPM_SIGNAL"));
        sql.add(getDDLCreateIndex("BPM_SIGNAL", "IX_MESSAGE_SELECTORS", "MESSAGE_SELECTORS"));
        return sql;
    }

}
