package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddTokenErrorDataPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("ERROR_DATE", Types.TIMESTAMP)));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("ERROR_MESSAGE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        return sql;
    }

}
