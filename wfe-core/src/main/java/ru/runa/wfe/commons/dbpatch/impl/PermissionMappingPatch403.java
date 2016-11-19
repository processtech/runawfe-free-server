package ru.runa.wfe.commons.dbpatch.impl;

import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

import com.google.common.collect.Lists;

public class PermissionMappingPatch403 extends DBPatch {

    @Override
    protected void applyPatch(Session session) throws Exception {
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLRemoveIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_IDENTIFIABLE"));
        sql.add(getDDLCreateUniqueKey("PERMISSION_MAPPING", "UQ_MAPPINGS", "IDENTIFIABLE_ID", "TYPE_ID", "MASK", "EXECUTOR_ID"));
        return sql;
    }

}
