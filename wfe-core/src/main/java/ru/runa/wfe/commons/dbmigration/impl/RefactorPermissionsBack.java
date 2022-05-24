package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * See #1586 (reverting some significant changes in permissions UI), #1586-10.
 */
public class RefactorPermissionsBack extends DbMigration {

    private static class PMatch {
        final String type;
        final boolean hasIds;
        final String[] perms;

        PMatch (String type, boolean hasIds, String... perms) {
            this.type = type;
            this.hasIds = hasIds;
            this.perms = perms;
        }
    }

    private PMatch[] pMatches;
    private List<String> allTypes;

    public RefactorPermissionsBack() {
        // ATTENTION!!! This duplicates visible permission lists in ApplicablePermissions configuration.
        pMatches = new PMatch[] {
                new PMatch("BOTSTATIONS", false, "READ", "UPDATE_PERMISSIONS", "UPDATE"),
                new PMatch("DEFINITION", true, "READ", "UPDATE_PERMISSIONS", "UPDATE", "DELETE", "START_PROCESS", "READ_PROCESS", "CANCEL_PROCESS"),
                new PMatch("EXECUTOR", true, "READ", "UPDATE_PERMISSIONS", "UPDATE", "UPDATE_ACTOR_STATUS", "VIEW_TASKS"),
                new PMatch("PROCESS", true, "READ", "UPDATE_PERMISSIONS", "CANCEL"),
                new PMatch("RELATION", true, "READ", "UPDATE_PERMISSIONS", "UPDATE"),
                new PMatch("RELATIONS", false, "READ", "UPDATE_PERMISSIONS", "UPDATE"),
                new PMatch("REPORT", true, "READ", "UPDATE_PERMISSIONS", "UPDATE"),
                new PMatch("REPORTS", false, "READ", "UPDATE_PERMISSIONS", "UPDATE"),
                new PMatch("SYSTEM", false, "READ", "UPDATE_PERMISSIONS", "LOGIN", "CHANGE_SELF_PASSWORD", "CREATE_EXECUTOR", "CREATE_DEFINITION", "VIEW_LOGS"),
        };

        allTypes = new ArrayList<>(pMatches.length);
        for (PMatch pm : pMatches) {
            allTypes.add(pm.type);
        }
    }


    @Override
    @SneakyThrows
    public void executeDML(Session session) {
//        if (true) throw new Exception("DEBUG STOP");
        session.doWork(conn -> {

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
            executeDML_step3(session, conn);
        } else {
            executeDML_fromV44(session, conn);
        }
        });
    }


    /**
     * Migrate 4.3.0 to 4.4.1. Moved 4.4.0's RefactorPermissionsStep3.executeDML() here and edited.
     * Note that RefactorPermissionStep4 (merging ACTOR and GROUP to EXECUTOR) is already done.
     *
     * Adjusts object_type and permission values for rm660, see https://rm.processtech.ru/attachments/download/1210.
     */
    @SneakyThrows
    private void executeDML_step3(Session session, Connection conn) {

        // Refill priveleged_mapping from scratch.
        {
            @SuppressWarnings("unchecked")
            List<Number> executorIds = session.createSQLQuery("select distinct executor_id from priveleged_mapping order by executor_id").list();

            session.createSQLQuery("delete from priveleged_mapping where 1=1").executeUpdate();

            SQLQuery qInsert = session.createSQLQuery("insert into priveleged_mapping(" + insertPkColumn() + "type, executor_id) values (" +
                    insertPkNextVal("priveleged_mapping") + ":type, :executorId)");
            for (String t : allTypes) {
                for (Number e : executorIds) {
                    qInsert.setParameter("type", t);
                    qInsert.setParameter("executorId", e);
                    qInsert.executeUpdate();
                }
            }
        }


        // Update permission_mapping.type.
        try (val stmt = conn.createStatement()) {
            stmt.executeUpdate("update permission_mapping set object_type = 'REPORTS' where object_type = 'REPORT' and object_id = 0");
            stmt.executeUpdate("update permission_mapping set object_type = 'BOTSTATIONS' where object_type = 'BOTSTATION'");
            stmt.executeUpdate("update permission_mapping set object_type = 'RELATIONS' where object_type = 'RELATIONGROUP'");
        }


        // Merge permissions.
        // - on EXECUTOR: READ, LIST_GROUP into READ;
        // - on EXECUTOR: ADD_TO_GROUP, REMOVE_FROM_GROUP, UPDATE_EXECUTOR into UPDATE;
        {
            @AllArgsConstructor
            @EqualsAndHashCode
            class FoundPermission {
                Long executorId;
                Long objectId;
            }

            @AllArgsConstructor
            @EqualsAndHashCode
            class MapKey {
                String objectType;
                String permission;
            }

            class MapValue {
                String[] sourcePermissions;
                HashSet<FoundPermission> found = new HashSet<>(1000);

                MapValue(String... sourcePermissions) {
                    this.sourcePermissions = sourcePermissions;
                }
            }

            // key.permission is target permission to merge into, value.sourcePermissions are to merge from.
            Map<MapKey, MapValue> mapTargetToSources = new HashMap<MapKey, MapValue>() {{
                put(new MapKey("EXECUTOR", "READ"), new MapValue("READ", "LIST_GROUP"));
                put(new MapKey("EXECUTOR", "UPDATE"), new MapValue("ADD_TO_GROUP", "REMOVE_FROM_GROUP", "UPDATE_EXECUTOR"));
            }};

            Map<MapKey, MapKey> mapSourceToTarget = new HashMap<>(mapTargetToSources.size());
            // TODO TMP
            // for (val kv : mapTargetToSources.entrySet()) {
            // MapKey k = kv.getKey();
            // for (String p : kv.getValue().sourcePermissions) {
            // mapSourceToTarget.put(new MapKey(k.objectType, p), k);
            // }
            // }

            val filterObjectTypes = new ArrayList<String>(mapTargetToSources.size());
            val filterPermissions = new ArrayList<String>(mapTargetToSources.size() * 10);
            // TODO TMP
            // for (val kv : mapTargetToSources.entrySet()) {
            // filterObjectTypes.add(kv.getKey().objectType);
            // filterPermissions.addAll(Arrays.asList(kv.getValue().sourcePermissions));
            // }

            try (
                    Statement q = conn.createStatement();
                    PreparedStatement qDelete = conn.prepareStatement("delete from permission_mapping where id = ?");
                    PreparedStatement qUpdate = conn.prepareStatement("update permission_mapping set permission = ? where id = ?");
            ) {
                val rs = q.executeQuery("select id, executor_id, object_type, object_id, permission " +
                        "from permission_mapping " +
                        "where object_type in ('" + StringUtils.join(filterObjectTypes, "','") + "') " +
                        "and permission in ('" + StringUtils.join(filterPermissions, "','") + "')");
                while (rs.next()) {
                    String permission = rs.getString(5);
                    val key = mapSourceToTarget.get(new MapKey(rs.getString(3), permission));
                    if (key == null) {
                        // Ignore unknown combination.
                        continue;
                    }
                    val value = mapTargetToSources.get(key);
                    val found = new FoundPermission(rs.getLong(2), rs.getLong(4));
                    val id = rs.getLong(1);

                    if (value.found.contains(found)) {
                        qDelete.setLong(1, id);
                        qDelete.executeUpdate();
                    } else {
                        value.found.add(found);
                        if (!permission.equals(key.permission)) {
                            qUpdate.setString(1, key.permission);
                            qUpdate.setLong(2, id);
                            qUpdate.executeUpdate();
                        }
                    }
                }
            }
        }


        // Updates without merging:
        {
            String[] specialQueries = new String[] {
                    "update permission_mapping set permission = 'UPDATE' " +
                            "where permission in ('BOT_STATION_CONFIGURE', 'REDEPLOY_DEFINITION', 'UPDATE_RELATION', 'DEPLOY_REPORT')",

                    "update permission_mapping set permission = 'DELETE' where object_type = 'DEFINITION' and permission = 'UNDEPLOY_DEFINITION'",

                    "update permission_mapping set permission = 'VIEW_TASKS' " +
                            "where object_type = 'EXECUTOR' and permission in ('VIEW_ACTOR_TASKS', 'VIEW_GROUP_TASKS')",

                    "update permission_mapping set permission = 'CANCEL' where object_type = 'PROCESS' and permission = 'CANCEL_PROCESS'",

                    "update permission_mapping set permission = 'LOGIN' where object_type = 'SYSTEM' and permission = 'LOGIN_TO_SYSTEM'",
                    "update permission_mapping set permission = 'CREATE_DEFINITION' where object_type='SYSTEM' and permission = 'DEPLOY_DEFINITION'",
            };
            for (String q : specialQueries) {
                session.createSQLQuery(q).executeUpdate();
            }
        }


        deleteBadPermissionMappings(session);


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


    /**
     * Migrate 4.4.0 to 4.4.1.
     */
    @SneakyThrows
    private void executeDML_fromV44(Session session, Connection conn) {
        // Replace all LIST permissions with READ.
        // Since some objects may have both permissions, have to re-insert them to avoid UK violations. To reduce memory usage,
        // process only object types which can have both permissions and which we are not going to delete below: DEFINITION, EXECUTOR, PROCESS.
        //
        // Note on EXECUTOR. RefactorPermissionsStep4 was not touched because:
        // 1. after RefactorPermissionsStep3, both ACTOR and GROUP had same allowed permission lists (see pMatches array there);
        // 2. method executeDML() above uses RefactorPermissionsStep4's result to determine which wfe version we are migrating from.
        {
            @AllArgsConstructor
            @EqualsAndHashCode
            class Row {
                Long executorId;
                String objectType;
                Long objectId;
            }
            val rows = new LinkedHashSet<Row>(1000);
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
                    stmt.executeUpdate();
                }
            }
        }


        // Do everything else.
        {
            String[] specialQueries = new String[]{
                    // Delete UPDATE_STATUS perission for groups, see #1586-27.
                    "delete from permission_mapping where permission='UPDATE_STATUS' and object_id in (select id from executor where discriminator='Y')",

                    "update permission_mapping set permission = 'UPDATE' " +
                            "where object_type in ('BOTSTATIONS', 'RELATIONS', 'REPORT', 'REPORTS') and permission = 'ALL'",

                    "update permission_mapping set permission = 'START_PROCESS' where object_type='DEFINITION' and permission = 'START'",

                    "update permission_mapping set permission = 'UPDATE_ACTOR_STATUS' where object_type='EXECUTOR' and permission = 'UPDATE_STATUS'",

                    "update permission_mapping set object_type = 'SYSTEM' where object_type = 'EXECUTORS' and permission = 'LOGIN'",

                    "update permission_mapping set permission = 'READ' where object_type='SYSTEM' and permission = 'ALL'",
                    "update permission_mapping set permission = 'VIEW_LOGS', object_type = 'SYSTEM' " +
                            "where object_type='LOGS' and permission = 'ALL'",
                    "update permission_mapping set permission = 'CREATE_DEFINITION', object_type = 'SYSTEM' " +
                            "where object_type='DEFINITIONS' and permission = 'CREATE'",
                    "update permission_mapping set permission = 'CREATE_EXECUTOR', object_type = 'SYSTEM' " +
                            "where object_type='EXECUTORS' and permission = 'CREATE'",
            };
            try (Statement stmt = conn.createStatement()) {
                for (String sql : specialQueries) {
                    stmt.executeUpdate(sql);
                }
            }
        }

        deleteBadPermissionMappings(session);

        {
            // fix RELATION if not exists
            String type = "RELATION";
            Number count = (Number) session.createSQLQuery("select count(*) from priveleged_mapping where type = :type").setParameter("type", type)
                    .uniqueResult();
            if (count.intValue() == 0) {
                List<Number> executorIds = session.createSQLQuery("select distinct executor_id from priveleged_mapping order by executor_id").list();
                SQLQuery qInsert = session.createSQLQuery("insert into priveleged_mapping(" + insertPkColumn() + "type, executor_id) values ("
                        + insertPkNextVal("priveleged_mapping") + ":type, :executorId)");
                for (Number e : executorIds) {
                    qInsert.setParameter("type", type);
                    qInsert.setParameter("executorId", e);
                    qInsert.executeUpdate();
                }
            }
        }
    }

    /**
     * Delete permission_mapping rows which still contain illegal (object type, permission name) combinations.
     */
    private void deleteBadPermissionMappings(Session session) {
        session.createSQLQuery("delete from permission_mapping where object_type not in (:types)").setParameterList("types", allTypes).executeUpdate();
        session.createSQLQuery("delete from priveleged_mapping where        type not in (:types)").setParameterList("types", allTypes).executeUpdate();

        Query qDeleteWithIds = session.createSQLQuery("delete from permission_mapping where (object_type = :type) and object_id <> 0");
        Query qDeleteWithoutIds = session.createSQLQuery("delete from permission_mapping where (object_type = :type) and object_id = 0");
        Query qDeleteBadPerms = session.createSQLQuery("delete from permission_mapping where (object_type = :type) and (permission not in (:permissions))");
        for (PMatch pm : pMatches) {
            (pm.hasIds ? qDeleteWithoutIds : qDeleteWithIds).setParameter("type", pm.type).executeUpdate();
            qDeleteBadPerms.setParameter("type", pm.type).setParameterList("permissions", pm.perms).executeUpdate();
        }
    }
}
