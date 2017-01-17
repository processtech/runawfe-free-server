package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;


public class AddStartFromCommentColumn extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("START_FROM_COMMENT", dialect.getTypeName(Types.BIGINT))));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }

}
