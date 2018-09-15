package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * @author Alex Chernyshev
 */
public class AddProcessAndTokenExecutionStatusPatch extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS", new ColumnDef("EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        session.createSQLQuery("UPDATE BPM_PROCESS SET EXECUTION_STATUS='ENDED' WHERE END_DATE IS NOT NULL").executeUpdate();
        session.createSQLQuery("UPDATE BPM_PROCESS SET EXECUTION_STATUS='ACTIVE' WHERE END_DATE IS NULL").executeUpdate();
        session.createSQLQuery("UPDATE BPM_TOKEN SET EXECUTION_STATUS='ENDED' WHERE END_DATE IS NOT NULL").executeUpdate();
        session.createSQLQuery("UPDATE BPM_TOKEN SET EXECUTION_STATUS='ACTIVE' WHERE END_DATE IS NULL").executeUpdate();
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLModifyColumnNullability("BPM_PROCESS", "EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false));
        sql.add(getDDLModifyColumnNullability("BPM_TOKEN", "EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false));
        return sql;
    }
}
