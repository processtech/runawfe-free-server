package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;
import ru.runa.wfe.commons.dbpatch.DbPatch;

public class AddSubprocessBindingDatePatch extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("SUBPROCESS_BINDING_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

}