package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import org.hibernate.Session;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class PerformancePatch401 extends DbMigration {

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
    protected void executeDDLBefore() {
        executeDDL(
                getDDLCreateColumn("PERMISSION_MAPPING", new ColumnDef("TYPE_ID", Types.BIGINT)),
                getDDLDropColumn("PERMISSION_MAPPING", "VERSION")
        );
    }

    @Override
    protected void executeDDLAfter() {
        executeDDL(
                getDDLDropIndex("PERMISSION_MAPPING", "IX_PERMISSION_IDENTIFIABLE_ID"),
                getDDLDropIndex("PERMISSION_MAPPING", "IX_PERMISSION_TYPE"),
                getDDLDropIndex("PERMISSION_MAPPING", "IX_PERMISSION_EXECUTOR"),
                getDDLDropColumn("PERMISSION_MAPPING", "TYPE"),
                getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_EXECUTOR", "EXECUTOR_ID", "TYPE_ID", "MASK", "IDENTIFIABLE_ID"),
                getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_IDENTIFIABLE", "IDENTIFIABLE_ID", "TYPE_ID", "MASK", "EXECUTOR_ID")
        );
        //
        if (dbType == DbType.MSSQL) {
            executeUpdates("CREATE NONCLUSTERED INDEX IX_VARIABLE_NAME ON BPM_VARIABLE (NAME) INCLUDE (PROCESS_ID, STRINGVALUE)");
        } else {
            executeDDL(getDDLCreateIndex("BPM_VARIABLE", "IX_VARIABLE_NAME", "NAME"));
        }
    }

    @Override
    public void executeDML(Session session) {
        for (Match m : matches) {
            String q = "UPDATE PERMISSION_MAPPING SET TYPE_ID=" + m.typeId + " WHERE TYPE='" + m.typeName + "'";
            log.info("Updated permission mappings (" + m.typeName + " --> " + m.typeId + "): " + session.createSQLQuery(q).executeUpdate());
        }
    }
}
