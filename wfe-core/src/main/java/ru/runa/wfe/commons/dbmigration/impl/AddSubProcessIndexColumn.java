package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbmigration.DbPatch;

public class AddSubProcessIndexColumn extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("SUBPROCESS_INDEX", dialect.getTypeName(Types.INTEGER))));
        return sql;
    }

}
