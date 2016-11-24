package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddDeploymentLockPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("LOCK_USER_ID", dialect.getTypeName(Types.BIGINT), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("LOCK_DATE", dialect.getTypeName(Types.DATE), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION",
                new ColumnDef("LOCK_FOR_ALL", dialect.getTypeName(Types.BOOLEAN), false).setDefaultValue("false")));
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateForeignKey("BPM_PROCESS_DEFINITION", "FK_DEFINITION_LOCK_USER", "LOCK_USER_ID", "EXECUTOR", "ID"));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
