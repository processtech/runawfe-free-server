package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddDeploymentLockPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION",
                new ColumnDef("LOCK_USER_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("LOCK_DATE", dialect.getTypeName(Types.DATE), true)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
