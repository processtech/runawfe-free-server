package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddAggregatedTaskIndexPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_AGGLOG_TASKS", new ColumnDef("TASK_INDEX", dialect.getTypeName(Types.INTEGER))));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
