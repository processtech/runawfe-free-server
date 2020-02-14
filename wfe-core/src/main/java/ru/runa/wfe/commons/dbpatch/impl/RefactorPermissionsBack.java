package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.val;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * See #1586 (reverting some significant changes in permissions UI), #1586-10.
 */
public class RefactorPermissionsBack extends DbPatch {

    @Override
    @SneakyThrows
    public void executeDML(Session session) {
        Connection conn = session.connection();

        // Are we working with RunaWFE 4.3.0 (original RefactorPermissionsStep4 is not yet applied) or 4.4.0?
        boolean isV43;
        try (Statement stmt = conn.createStatement()) {
            // No "limit 1", because Oracle does not understand it.
            val rs = stmt.executeQuery("select 1 from priveleged_mapping where type='ACTOR'");
            isV43 = rs.next();
        }

        if (isV43) {
            // Postoned RefactorPermissionsStep4 execution to detect (above) which version we are migrating from.
            // But now we execute it before even former RefactorPermissionsStep3, because this simplifles RefactorPermissionsStep3.
            executeDML_step4(session);
            executeDML_step3(session);
        } else {
            executeDML_fromV44(conn);
        }
    }


    /**
     * Migrate 4.3.0 to 4.4.1. Moved 4.4.0's RefactorPermissionsStep3.executeDML() here and edited.
     * Note that RefactorPermissionStep4 (merging ACTOR and GROUP to EXECUTOR) is already done.
     *
     * Adjusts object_type and permission values for rm660, see https://rm.processtech.ru/attachments/download/1210.
     */
    @SneakyThrows
    private void executeDML_step3(Session session) {

        class PMatch {
            private final String type;
            private final boolean hasIds;
            private final String[] perms;

            private PMatch (String type, boolean hasIds, String... perms) {
                this.type = type;
                this.hasIds = hasIds;
                this.perms = perms;
            }
        }

        // ATTENTION!!! This duplicates visible permission lists in ApplicablePermissions configuration.
        PMatch[] pMatches = new PMatch[] {
                new PMatch("BOTSTATIONS", false, "ALL"),
                new PMatch("DEFINITION", true, "ALL", "READ", "UPDATE", "DELETE", "START", "READ_PROCESS", "CANCEL_PROCESS"),
                new PMatch("EXECUTOR", true, "READ", "VIEW_TASKS", "UPDATE", "UPDATE_STATUS", "DELETE"),
                new PMatch("PROCESS", true, "ALL", "READ", "CANCEL"),
                new PMatch("RELATIONS", false, "ALL"),
                new PMatch("REPORT", true, "ALL", "READ"),
                new PMatch("REPORTS", false, "ALL", "READ"),
                new PMatch("SYSTEM", false, "READ", "LOGIN", "CREATE_EXECUTOR", "CREATE_DEFINITION", "READ_LOGS"),
        };

        List<String> allTypes = new ArrayList<>(pMatches.length);
        for (PMatch pm : pMatches) {
            allTypes.add(pm.type);
        }


        // Refill priveleged_mapping from scratch.
        {
            @SuppressWarnings("unchecked")
            List<Number> executorIds = session.createSQLQuery("select distinct executor_id from priveleged_mapping order by executor_id").list();

            String idName, idValue;
            switch (dbType) {
                case ORACLE:
                    idName = "id, ";
                    idValue = "seq_priveleged_mapping.nextval, ";
                    break;
                case POSTGRESQL:
                    idName = "id, ";
                    idValue = "nextval('seq_priveleged_mapping'), ";
                    break;
                default:
                    idName = "";
                    idValue = "";
            }
            SQLQuery qInsert = session.createSQLQuery("insert into priveleged_mapping(" + idName + "type, executor_id) values (" + idValue +
                    ":type, :executorId)");

            session.createSQLQuery("delete from priveleged_mapping where 1=1").executeUpdate();
            for (String t : allTypes) {
                for (Number e : executorIds) {
                    qInsert.setParameter("type", t);
                    qInsert.setParameter("executorId", e);
                    qInsert.executeUpdate();
                }
            }
        }


        // Update permission_mapping.type.
        {
            class TMatch {
                private final String oldType;
                private final String newType;
                private final boolean clearObjectId;

                private TMatch(String oldType, String newType, boolean clearObjectId) {
                    this.oldType = oldType;
                    this.newType = newType;
                    this.clearObjectId = clearObjectId;
                }
            }

            TMatch[] tMatches = {
                    new TMatch("BOTSTATION", "BOTSTATIONS", true),
                    // RELATION & RELATION_PAIR will be just deleted; it simpler than deal with possible UK violations, and it's also more correct:
                    new TMatch("RELATIONGROUP", "RELATIONS", true),
                    // REPORT will now refer to report instance, not to list of reports:
                    new TMatch("REPORT", "REPORTS", true),
            };
            HashMap<String, TMatch> tMap = new HashMap<>(tMatches.length);
            for (TMatch m : tMatches) {
                tMap.put(m.oldType, m);
            }

            // Delete is needed to avoid possible UK violation by following update.
            SQLQuery qDelete = session.createSQLQuery("delete from permission_mapping where object_type = :objectType and object_id = :objectId and permission = :permission and executor_id = :executorId");
            SQLQuery qUpdate = session.createSQLQuery("update permission_mapping set object_type = :objectType, object_id = :objectId where id = :id");

            // I tried iterate(), but got "SQL queries do not currently support iteration".
            @SuppressWarnings("unchecked")
            List<Object[]> rows = session
                    .createSQLQuery("select id, object_type, object_id, permission, executor_id from permission_mapping where object_type in (:types)")
                    .setParameterList("types", tMap.keySet())
                    .list();
            for (Object[] row : rows) {
                long id = ((Number)row[0]).longValue();
                String objectType = (String)row[1];
                long objectId = ((Number)row[2]).longValue();
                String permission = (String)row[3];
                long executorId = ((Number)row[4]).longValue();

                TMatch m = tMap.get(objectType);
                if (m.clearObjectId) {
                    objectId = 0;
                }

                qDelete.setParameter("objectType", m.newType);
                qDelete.setParameter("objectId", objectId);
                qDelete.setParameter("permission", permission);
                qDelete.setParameter("executorId", executorId);
                qDelete.executeUpdate();

                qUpdate.setParameter("objectType", m.newType);
                qUpdate.setParameter("objectId", objectId);  // Reset to 0 if m.clearObjectId, unchanged otherwise.
                qUpdate.setParameter("id", id);
                qUpdate.executeUpdate();
            }
        }


        // Merge LIST_GROUP permission into READ on EXECUTOR-s.
        // This can be done with single INSERT SELECT WHERE NOT EXISTS (), but i'm afraid it won't work on all SQL servers.
        {
            @AllArgsConstructor
            @EqualsAndHashCode(onlyExplicitlyIncluded = true)
            class HasPermission {
                long id;

                @EqualsAndHashCode.Include
                Long executorId;

                @EqualsAndHashCode.Include
                Long objectId;
            }
            val haveReadPermission = new HashSet<HasPermission>(1000);
            val haveListPermission = new HashSet<HasPermission>(1000);

            val conn = session.connection();
            try (Statement q = conn.createStatement()) {
                val rs = q.executeQuery("select id, executor_id, object_id, permission from permission_mapping " +
                        "where object_type = 'EXECUTOR' and permission in ('READ', 'LIST_GROUP')");
                while (rs.next()) {
                    (rs.getString(4).equals("READ") ? haveReadPermission : haveListPermission)
                            .add(new HasPermission(rs.getLong(1), rs.getLong(2), rs.getLong(3)));
                }
            }

            try (
                    PreparedStatement qUpdate = conn.prepareStatement("update permission_mapping set permission = 'READ' where id = ?");
                    PreparedStatement qDelete = conn.prepareStatement("delete from permission_mapping where id = ?")
            ) {
                for (HasPermission p : haveListPermission) {
                    val q = haveReadPermission.contains(p) ? qDelete : qUpdate;
                    q.setLong(1, p.id);
                    q.executeUpdate();
                }
            }
        }


        // Update permission_mapping.permission. ONLY NAROWWING TRANSFORMATIONS, NO "ALL" ESCALATION.
        // ATTENTION!!! Possible UK violations are not avoided!
        {
            String[] specialQueries = new String[] {
                    "update permission_mapping set permission = 'ALL' where object_type = 'BOTSTATIONS' and permission = 'BOT_STATION_CONFIGURE'",

                    "update permission_mapping set permission = 'UPDATE' where object_type = 'EXECUTOR' and permission = 'ADD_TO_GROUP'",
                    "delete from permission_mapping where object_type = 'EXECUTOR' and permission = 'REMOVE_FROM_GROUP'",
                    "update permission_mapping set permission = 'UPDATE_STATUS' where object_type = 'EXECUTOR' and permission = 'UPDATE_ACTOR_STATUS'",
                    "update permission_mapping set permission = 'VIEW_TASKS' where object_type = 'EXECUTOR' and permission in ('VIEW_ACTOR_TASKS', 'VIEW_GROUP_TASKS')",

                    "update permission_mapping set permission = 'CANCEL' where object_type = 'PROCESS' and permission = 'CANCEL_PROCESS'",

                    "update permission_mapping set permission = 'LOGIN' where object_type = 'SYSTEM' and permission = 'LOGIN_TO_SYSTEM'",
                    "update permission_mapping set permission = 'CREATE_DEFINITION' where object_type='SYSTEM' and permission = 'DEPLOY_DEFINITION'",
                    "update permission_mapping set permission = 'READ_LOGS' where object_type='SYSTEM' and permission = 'VIEW_LOGS'",
            };
            for (String q : specialQueries) {
                session.createSQLQuery(q).executeUpdate();
            }
        }


        // Delete permission_mapping rows which still contain illegal (object type, permission name) combinations.
        {
            session.createSQLQuery("delete from permission_mapping where object_type not in (:types)")
                    .setParameterList("types", allTypes)
                    .executeUpdate();

            Query qDeleteWithIds = session.createSQLQuery("delete from permission_mapping where (object_type = :type) and object_id <> 0");
            Query qDeleteWithoutIds = session.createSQLQuery("delete from permission_mapping where (object_type = :type) and object_id = 0");
            Query qDeleteBadPerms = session.createSQLQuery("delete from permission_mapping where (object_type = :type) and (permission not in (:permissions))");
            for (PMatch pm : pMatches) {
                (pm.hasIds ? qDeleteWithoutIds : qDeleteWithIds).setParameter("type", pm.type).executeUpdate();
                qDeleteBadPerms.setParameter("type", pm.type).setParameterList("permissions", pm.perms).executeUpdate();
            }
        }


        // Delete old obsolete batch_permission categories.
        {
            session.createSQLQuery("delete from batch_presentation where category in (:categories)")
                    .setParameterList("categories", new String[] {
                            "listExecutorsWithoutBotStationPermission",
                            "listExecutorsWithoutPermissionsOnExecutorForm",
                            "listExecutorsWithoutPermissionsOnSystemForm",
                            "listExecutorsWithoutPermissionsOnDefinitionForm",
                            "listExecutorsWithoutPermissionsOnProcessForm",
                            "listExecutorsWithoutPermissionsOnRelationForm",
                            "listExecutorsWithoutPermissionsOnReportsForm",
                    })
                    .executeUpdate();
        }
    }


    /**
     * Migrate 4.4.0 to 4.4.1.
     */
    @SneakyThrows
    private void executeDML_fromV44(Connection conn) {
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

            try (PreparedStatement stmt = conn.prepareStatement("insert into permission_mapping (" + insertPkColumn() +
                    "executor_id, object_type, object_id, permission) values (" + insertPkNextVal("permission_mapping") + "?, ?, ?, 'READ')"
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
            // Same list as allTypes in executeDML_step3().
            val deleteTypesSqlSuffix = "not in ('BOTSTATIONS', 'DEFINITION', 'EXECUTOR', 'PROCESS', 'RELATIONS', 'REPORT', 'REPORTS', 'SYSTEM')";

            String[] specialQueries = new String[]{
                    "update permission_mapping set permission = 'READ' where object_type='SYSTEM' and permission = 'ALL'",
                    "update permission_mapping set object_type = 'SYSTEM' where object_type = 'EXECUTORS' and permission = 'LOGIN'",
                    "update permission_mapping set permission = 'CREATE_EXECUTOR', object_type = 'SYSTEM' where object_type='EXECUTORS' and permission = 'CREATE'",
                    "update permission_mapping set permission = 'CREATE_DEFINITION', object_type = 'SYSTEM' where object_type='DEFINITIONS' and permission = 'CREATE'",
                    "update permission_mapping set permission = 'READ_LOGS', object_type = 'SYSTEM' where object_type='LOGS' and permission = 'ALL'",
                    "delete from permission_mapping where object_type " + deleteTypesSqlSuffix,
                    "delete from priveleged_mapping where type " + deleteTypesSqlSuffix
            };
            try (Statement stmt = conn.createStatement()) {
                for (String sql : specialQueries) {
                    stmt.executeUpdate(sql);
                }
            }
        }
    }


    /**
     * Moved 4.4.0's RefactorPermissionsStep4.executeDML() here, unchanged.
     *
     * Types ACTOR and GROUP are merged into EXECUTOR for rm718.
     */
    private void executeDML_step4(Session session) {
        // Replace ACTOR and GROUP types with EXECUTOR in permission_mapping
        // Delete ACTOR and GROUP from priveleged_mapping
        {
            session.createSQLQuery("delete from permission_mapping where object_type = 'EXECUTOR'").executeUpdate();
            session.createSQLQuery("update permission_mapping set object_type = 'EXECUTOR' where object_type = 'ACTOR' or object_type = 'GROUP'")
                    .executeUpdate();
            session.createSQLQuery("delete from priveleged_mapping where type = 'EXECUTOR'").executeUpdate();
            session.createSQLQuery("delete from priveleged_mapping where type = 'GROUP'").executeUpdate();
            session.createSQLQuery("update priveleged_mapping set type = 'EXECUTOR' where type = 'ACTOR'").executeUpdate();
        }
    }

    private String insertPkColumn() {
        switch (dbType) {
            case ORACLE:
            case POSTGRESQL:
                return "id, ";
            default:
                return "";
        }
    }

    private String insertPkNextVal(String tableName) {
        switch (dbType) {
            case ORACLE:
                return "seq_" + tableName + ".nextval, ";
            case POSTGRESQL:
                return "nextval('seq_" + tableName + "'), ";
            default:
                return "";
        }
    }
}
