package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddAggregatedTaskIndexPatch extends DbMigration {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_AGGLOG_TASKS", new ColumnDef("TASK_INDEX", dialect.getTypeName(Types.INTEGER))));
        return sql;
    }

}
