package ru.runa.wfe.commons.dbpatch.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.dbpatch.DBPatch;

import java.sql.Types;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Pavel
 * Date: 19.04.16
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */
public class AddDueDateExpressionToJobAndTask extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_JOB", new ColumnDef("DUE_DATE_EXPRESSION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("DEADLINE_DATE_EXPRESSION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
