package ru.runa.wfe.commons.dbpatch.impl;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbpatch.DBPatch;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Iterator;

public class AddAssignDateColumn extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("ASSIGN_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        List taskIds = session.createSQLQuery("SELECT ID FROM BPM_TASK").list();
        for (Iterator it = taskIds.iterator(); it.hasNext();) {
            BigInteger taskId = (BigInteger) it.next();
            Object assignmentDateFromDB = session.createSQLQuery("SELECT ASSIGNMENT_DATE FROM BPM_AGGLOG_ASSIGNMENTS WHERE ASSIGNMENT_OBJECT_ID=" + taskId.longValue()).uniqueResult();
            SQLQuery query = session.createSQLQuery("UPDATE BPM_TASK SET ASSIGN_DATE=:assignDateTime WHERE ASSIGN_DATE IS NULL AND ID=:taskId");
            query.setParameter("assignDateTime", (Timestamp)assignmentDateFromDB);
            query.setParameter("taskId", taskId);
            query.executeUpdate();
        }
    }

}
