package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.security.SecuredObjectType;

import com.google.common.collect.Lists;

public class PerformancePatch401 extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLCreateColumn("PERMISSION_MAPPING", new ColumnDef("TYPE_ID", Types.BIGINT)));
        sql.add(getDDLRemoveColumn("PERMISSION_MAPPING", "VERSION"));
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLRemoveIndex("PERMISSION_MAPPING", "IX_PERMISSION_IDENTIFIABLE_ID"));
        sql.add(getDDLRemoveIndex("PERMISSION_MAPPING", "IX_PERMISSION_TYPE"));
        sql.add(getDDLRemoveIndex("PERMISSION_MAPPING", "IX_PERMISSION_EXECUTOR"));
        sql.add(getDDLRemoveColumn("PERMISSION_MAPPING", "TYPE"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_EXECUTOR", "EXECUTOR_ID", "TYPE_ID", "MASK", "IDENTIFIABLE_ID"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_IDENTIFIABLE", "IDENTIFIABLE_ID", "TYPE_ID", "MASK", "EXECUTOR_ID"));
        //
        if (dbType == DBType.MSSQL) {
            sql.add("CREATE NONCLUSTERED INDEX IX_VARIABLE_NAME ON BPM_VARIABLE (NAME) INCLUDE (PROCESS_ID, STRINGVALUE)");
        } else {
            sql.add(getDDLCreateIndex("BPM_VARIABLE", "IX_VARIABLE_NAME", "NAME"));
        }
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        for (SecuredObjectType type : SecuredObjectType.values()) {
            String q = "UPDATE PERMISSION_MAPPING SET TYPE_ID=" + type.ordinal() + " WHERE TYPE='" + type.name() + "'";
            log.info("Updated permission mappings (" + type + "): " + session.createSQLQuery(q).executeUpdate());
        }
    }

}
