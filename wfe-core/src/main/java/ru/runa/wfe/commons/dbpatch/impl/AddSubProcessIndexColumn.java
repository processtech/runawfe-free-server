package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddSubProcessIndexColumn extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("SUBPROCESS_INDEX", dialect.getTypeName(Types.INTEGER))));
        return sql;
    }

}
