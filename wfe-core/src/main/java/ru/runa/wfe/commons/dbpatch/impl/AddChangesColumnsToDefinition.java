package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;


public class AddChangesColumnsToDefinition extends DBPatch {
    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("VERSION_DATE", dialect.getTypeName(Types.TIMESTAMP), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("VERSION_AUTHOR", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("VERSION_COMMENT", dialect.getTypeName(Types.VARCHAR, 4096, 4096, 4096), true)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
