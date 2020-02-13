package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
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

        // Are we working with RunaWFE 4.3.0 (RefactorPermissionsStep4 is not yet applied) or 4.4.0?
        boolean isV43;
        try (Statement stmt = conn.createStatement()) {
            // No "limit 1", because Oracle does not understand it.
            val rs = stmt.executeQuery("select 1 from priveleged_mapping where type='ACTOR'");
            isV43 = rs.next();
        }

        if (isV43) {
            executeDML_v43(session);
        } else {
            executeDML_v44(conn);
        }
    }


    /**
     * Migrate 4.3.0 to 4.4.1 (moved old RefactorPermissionsStep3 here and edited).
     *
     * Adjusts object_type and permission values for rm660, see https://rm.processtech.ru/attachments/download/1210.
     */
    // TODO "update permission_mapping set permission = 'CREATE_DEFINITION' where object_type='SYSTEM' and permission = 'DEPLOY_DEFINITION'"
    // TODO "update permission_mapping set permission = 'READ_LOGS' where object_type='SYSTEM' and permission = 'VIEW_LOGS'"
    private void executeDML_v43(Session session) {

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
                new PMatch("ACTOR", true, "LIST", "READ", "VIEW_TASKS", "UPDATE", "UPDATE_STATUS", "DELETE"),
                new PMatch("BOTSTATIONS", false, "ALL"),
                new PMatch("DATAFILE", false, "ALL"),
                new PMatch("DEFINITION", true, "ALL", "LIST", "READ", "START", "READ_PROCESS", "CANCEL_PROCESS", "UPDATE"),
                new PMatch("DEFINITIONS", false, "ALL", "LIST", "READ", "START", "READ_PROCESS", "CANCEL_PROCESS", "CREATE", "UPDATE"),
                new PMatch("ERRORS", false, "ALL"),
                new PMatch("EXECUTORS", false, "ALL", "LOGIN", "LIST", "READ", "VIEW_TASKS", "CREATE", "UPDATE", "UPDATE_STATUS", "UPDATE_SELF", "DELETE"),
                new PMatch("GROUP", true, "LIST", "READ", "VIEW_TASKS", "UPDATE", "UPDATE_STATUS", "DELETE"),
                new PMatch("LOGS", false, "ALL"),
                new PMatch("PROCESSES", false, "ALL", "LIST", "READ", "CANCEL"),
                new PMatch("PROCESS", true, "ALL", "LIST", "READ", "CANCEL"),
                new PMatch("RELATIONS", false, "ALL"),
                new PMatch("REPORTS", false, "ALL", "LIST"),
                new PMatch("REPORT", true, "ALL", "LIST"),
                new PMatch("SCRIPTS", false, "ALL"),
                new PMatch("SUBSTITUTION_CRITERIAS", false, "ALL"),
                new PMatch("SYSTEM", false, "ALL"),
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


        // Update permission_mapping.permission. ONLY NAROWWING TRANSFORMATIONS, NO "ALL" ESCALATION.
        // ATTENTION!!! Possible UK violations are not avoided!
        {
            String[] specialQueries = new String[] {
                    "update permission_mapping set permission = 'LIST' where permission='READ'",
                    "update permission_mapping set permission = 'LOGIN', object_type = 'EXECUTORS' where object_type = 'SYSTEM' and permission = 'LOGIN_TO_SYSTEM'",
                    "update permission_mapping set permission = 'UPDATE_STATUS' where object_type = 'ACTOR' and permission = 'UPDATE_ACTOR_STATUS'",
                    "update permission_mapping set permission = 'VIEW_TASKS' where object_type = 'ACTOR' and permission = 'VIEW_ACTOR_TASKS'",
                    "update permission_mapping set permission = 'VIEW_TASKS' where object_type = 'GROUP' and permission = 'VIEW_GROUP_TASKS'",
                    "update permission_mapping set permission = 'READ' where object_type = 'GROUP' and permission = 'LIST_GROUP'",

                    "update permission_mapping set permission = 'UPDATE' where object_type = 'GROUP' and permission = 'ADD_TO_GROUP'",
                    "delete from permission_mapping where object_type = 'GROUP' and permission = 'REMOVE_FROM_GROUP'",

                    "update permission_mapping set permission = 'UPDATE' where object_type = 'BOTSTATIONS' and permission = 'BOT_STATION_CONFIGURE'",
                    "update permission_mapping set permission = 'START' where object_type = 'PROCESS' and permission = 'START_PROCESS'",
                    "update permission_mapping set permission = 'CANCEL' where object_type = 'PROCESS' and permission = 'CANCEL_PROCESS'",
                    "update permission_mapping set permission = 'CREATE', object_type = 'EXECUTORS' where object_type='SYSTEM' and permission = 'CREATE_EXECUTOR'",
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
