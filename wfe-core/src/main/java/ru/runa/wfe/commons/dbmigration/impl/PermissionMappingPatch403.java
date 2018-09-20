package ru.runa.wfe.commons.dbmigration.impl;

import java.util.List;

import ru.runa.wfe.commons.dbmigration.DbMigration;

import com.google.common.collect.Lists;

public class PermissionMappingPatch403 extends DbMigration {

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLDropIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_IDENTIFIABLE"));
        sql.add(getDDLCreateUniqueKey("PERMISSION_MAPPING", "UQ_MAPPINGS", "IDENTIFIABLE_ID", "TYPE_ID", "MASK", "EXECUTOR_ID"));
        return sql;
    }

}
