package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

/**
 * 
 * @author Alex Chernyshev
 */
public class AddProcessExecutionStatusPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS", new ColumnDef("EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_PROCESS SET EXECUTION_STATUS='ACTIVE'");
        updateQuery.executeUpdate();

    }
}
