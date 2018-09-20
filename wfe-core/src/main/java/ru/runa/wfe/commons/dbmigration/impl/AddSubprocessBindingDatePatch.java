package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSubprocessBindingDatePatch extends DbMigration {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("SUBPROCESS_BINDING_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

}