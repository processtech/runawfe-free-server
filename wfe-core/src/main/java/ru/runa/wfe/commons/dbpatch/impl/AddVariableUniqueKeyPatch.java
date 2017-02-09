package ru.runa.wfe.commons.dbpatch.impl;

import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

import com.google.common.collect.Lists;

public class AddVariableUniqueKeyPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLCreateUniqueKey("BPM_VARIABLE", "UK_VARIABLE_PROCESS", "PROCESS_ID", "NAME"));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
