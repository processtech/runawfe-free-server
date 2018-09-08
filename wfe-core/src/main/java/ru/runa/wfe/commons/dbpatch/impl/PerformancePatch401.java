package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.dbpatch.DbPatch;

import com.google.common.collect.Lists;

public class PerformancePatch401 extends DbPatch {

    // Cannot rely on model in migrations, even on enums:
    // SecuredObjectType evolved to extensible pseudo-enum without ordinal() method.
    private static class Match {
        int typeId;
        String typeName;

        public Match(int typeId, String typeName) {
            this.typeId = typeId;
            this.typeName = typeName;
        }
    }

    private static Match[] matches = {
            new Match(0, "NONE"),
            new Match(1, "SYSTEM"),
            new Match(2, "BOTSTATION"),
            new Match(3, "ACTOR"),
            new Match(4, "GROUP"),
            new Match(5, "RELATION"),
            new Match(6, "RELATIONGROUP"),
            new Match(7, "RELATIONPAIR"),
            new Match(8, "DEFINITION"),
            new Match(9, "PROCESS"),
            new Match(10, "REPORT"),
    };

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
        if (dbType == DbType.MSSQL) {
            sql.add("CREATE NONCLUSTERED INDEX IX_VARIABLE_NAME ON BPM_VARIABLE (NAME) INCLUDE (PROCESS_ID, STRINGVALUE)");
        } else {
            sql.add(getDDLCreateIndex("BPM_VARIABLE", "IX_VARIABLE_NAME", "NAME"));
        }
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        for (Match m : matches) {
            String q = "UPDATE PERMISSION_MAPPING SET TYPE_ID=" + m.typeId + " WHERE TYPE='" + m.typeName + "'";
            log.info("Updated permission mappings (" + m.typeName + " --> " + m.typeId + "): " + session.createSQLQuery(q).executeUpdate());
        }
    }

}
