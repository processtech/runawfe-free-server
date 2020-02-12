package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * See #5586 (reverting some significant changes in permissions UI), #1586-10.
 */
public class RefactorPermissionsBack extends DbPatch {

    @Override
    @SneakyThrows
    public void executeDML(Session session) {
        Connection conn = session.connection();

        // Are we working with RunaWFE 4.3.0 (RefactorPermissionsStep4 is not yet applied) or 4.4.0?
        boolean isV43;
        try (Statement stmt = conn.createStatement()) {
            // No "limit 1", because Oracle does not understand it.
            val rs = stmt.executeQuery("select 1 from priveleged_mapping where type='ACTOR'");
            isV43 = rs.next();
        }

        if (isV43) {
            executeDML_v43(conn);
        } else {
            executeDML_v44(conn);
        }
    }


    /**
     * Migrate 4.3.0 to 4.4.1 (old RefactorPermissionsStep3 + old RefactorPermissionsStep4).
     */
    // TODO "update permission_mapping set permission = 'CREATE_DEFINITION' where object_type='SYSTEM' and permission = 'DEPLOY_DEFINITION'"
    // TODO "update permission_mapping set permission = 'READ_LOGS' where object_type='SYSTEM' and permission = 'VIEW_LOGS'"
    private void executeDML_v43(Connection conn) {

    }


    /**
     * Migrate 4.4.0 to 4.4.1.
     */
    @SneakyThrows
    private void executeDML_v44(Connection conn) {
        // Replace all LIST permissions with READ.
        // Since some objects may have both permissions, have to re-insert them to avoid UK violations. To reduce memory usage,
        // process only object types which can have both permissions and which we are not going to delete below: DEFINITION, EXECUTOR, PROCESS.
        //
        // Note on EXECUTOR. RefactorPermissionsStep4 was not touched because:
        // 1. after RefactorPermissionsStep3, both ACTOR and GROUP had same allowed permission lists (see pMatches array there);
        // 2. method executeDML() above uses RefactorPermissionsStep4's result to determine which wfe version we are migrating from.
        {
            @AllArgsConstructor
            class Row {
                Long executorId;
                String objectType;
                Long objectId;
            }
            val rows = new ArrayList<Row>(1000);
            val sqlSuffix = "from permission_mapping where object_type in ('DEFINITION', 'EXECUTOR', 'PROCESS') and permission in ('LIST', 'READ')";
            try (Statement stmt = conn.createStatement()) {
                val rs = stmt.executeQuery("select executor_id, object_type, object_id " + sqlSuffix);
                while (rs.next()) {
                    rows.add(new Row(rs.getLong(1), rs.getString(2), rs.getLong(3)));
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete " + sqlSuffix);
            }

            String idName, idValue;
            switch (dbType) {
                case ORACLE:
                    // Bugfix: https://rm.processtech.ru/issues/637#note-15
                    idName = "id, ";
                    idValue = "seq_permission_mapping.nextval, ";
                    break;
                case POSTGRESQL:
                    // TODO Should we generate PK field with "default nextval('sequence_name')" instead?
                    //      Or even use BIGSERIAL instead of manual sequence creation?
                    idName = "id, ";
                    idValue = "nextval('seq_permission_mapping'), ";
                    break;
                default:
                    idName = "";
                    idValue = "";
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "insert into permission_mapping (" + idName + "executor_id, object_type, object_id, permission) values (" + idValue + "?, ?, ?, 'READ')"
            )) {
                for (Row r : rows) {
                    stmt.setLong(1, r.executorId);
                    stmt.setString(2, r.objectType);
                    stmt.setLong(3, r.objectId);
                }
            }
        }

        // Do everything else.
        {
            String[] specialQueries = new String[]{
                    "update permission_mapping set permission = 'READ' where object_type='SYSTEM' and permission = 'ALL'",
                    "update permission_mapping set object_type = 'SYSTEM' where object_type = 'EXECUTORS' and permission = 'LOGIN'",
                    "update permission_mapping set permission = 'CREATE_EXECUTOR', object_type = 'SYSTEM' where object_type='EXECUTORS' and permission = 'CREATE'",
                    "update permission_mapping set permission = 'CREATE_DEFINITION', object_type = 'SYSTEM' where object_type='DEFINITIONS' and permission = 'CREATE'",
                    "update permission_mapping set permission = 'READ_LOGS', object_type = 'SYSTEM' where object_type='LOGS' and permission = 'ALL'",
                    "delete from permission_mapping where object_type in ('DEFINITIONS', 'EXECUTORS', 'PROCESSES', 'LOGS', 'SUBSTITUTION_CRITERIAS')"
            };
            try (Statement stmt = conn.createStatement()) {
                for (String sql : specialQueries) {
                    stmt.executeUpdate(sql);
                }
            }
        }
    }
}
