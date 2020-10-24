package ru.runa.wfe.commons.dbmigration.impl;

import java.util.List;

import ru.runa.wfe.commons.dbmigration.DbPatch;

import com.google.common.collect.Lists;

public class AddVariableUniqueKeyPatch extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLCreateUniqueKey("BPM_VARIABLE", "UK_VARIABLE_PROCESS", "PROCESS_ID", "NAME"));
        return sql;
    }

}
