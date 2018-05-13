/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.dbpatch.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbpatch.DBPatch;

/**
 * Adjusts object_type and permission values for rm660, see https://rm.processtech.ru/attachments/download/1210.
 */
public class RefactorPermissionsStep3 extends DBPatch {

    @Override
    public void executeDML(Session session) {

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
                    "update permission_mapping set permission = 'UPDATE' where object_type = 'GROUP' and permission in ('ADD_TO_GROUP', 'REMOVE_FROM_GROUP')",
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
}
