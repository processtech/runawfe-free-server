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
package ru.runa.wfe.commons.dbmigration.impl;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * This refactors permission_mapping table structure, to store object_type and permission as strings.
 * Since there is unique index with random constraint name, have to rename old table, create and fill new one,
 * then drop renamed old table.
 */
public class RefactorPermissionsStep1 extends DbMigration {

    // Since SecuredObjectType and Permission will evolve during refactoring, I cannot use them here.
    // So I have to go low-level to ordinal, name and maskPower.

    private static class TMatch {
        int ordinal;
        String name;

        TMatch(int ordinal, String name) {
            this.ordinal = ordinal;
            this.name = name;
        }
    }

    private static class PMatch {
        String type;
        long maskPower;
        String name;

        PMatch(String type, long maskPower, String name) {
            this.type = type;
            this.maskPower = maskPower;
            this.name = name;
        }
    }

    private static TMatch[] tMatches = {
            new TMatch(1, "SYSTEM"),
            new TMatch(2, "BOTSTATION"),
            new TMatch(3, "ACTOR"),
            new TMatch(4, "GROUP"),
            new TMatch(5, "RELATION"),
            new TMatch(6, "RELATIONGROUP"),
            new TMatch(7, "RELATIONPAIR"),
            new TMatch(8, "DEFINITION"),
            new TMatch(9, "PROCESS"),
            new TMatch(10, "REPORT")
    };

    // For first step, Permission instance names are choosen to match old localization keys.
    private static PMatch[] pMatches = {
            new PMatch("ACTOR", 0 /*Permission.READ*/, "READ"),
            new PMatch("ACTOR", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("ACTOR", 2 /*ExecutorPermission.UPDATE*/, "UPDATE_EXECUTOR"),
            new PMatch("ACTOR", 3 /*ActorPermission.UPDATE_STATUS*/, "UPDATE_ACTOR_STATUS"),
            new PMatch("ACTOR", 4 /*ActorPermission.VIEW_TASKS*/, "VIEW_ACTOR_TASKS"),

            new PMatch("GROUP", 0 /*Permission.READ*/, "READ"),
            new PMatch("GROUP", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("GROUP", 2 /*ExecutorPermission.UPDATE*/, "UPDATE_EXECUTOR"),
            new PMatch("GROUP", 3 /*GroupPermission.LIST_GROUP*/, "LIST_GROUP"),
            new PMatch("GROUP", 4 /*GroupPermission.ADD_TO_GROUP*/, "ADD_TO_GROUP"),
            new PMatch("GROUP", 5 /*GroupPermission.REMOVE_FROM_GROUP*/, "REMOVE_FROM_GROUP"),
            new PMatch("GROUP", 6 /*GroupPermission.VIEW_TASKS*/, "VIEW_GROUP_TASKS"),

            new PMatch("BOTSTATION", 0 /*Permission.READ*/, "READ"),
            new PMatch("BOTSTATION", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("BOTSTATION", 4 /*BotStationPermission.BOT_STATION_CONFIGURE*/, "BOT_STATION_CONFIGURE"),

            new PMatch("DEFINITION", 0 /*Permission.READ*/, "READ"),
            new PMatch("DEFINITION", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("DEFINITION", 2 /*DefinitionPermission.REDEPLOY_DEFINITION*/, "REDEPLOY_DEFINITION"),
            new PMatch("DEFINITION", 3 /*DefinitionPermission.UNDEPLOY_DEFINITION*/, "UNDEPLOY_DEFINITION"),
            new PMatch("DEFINITION", 4 /*DefinitionPermission.START_PROCESS*/, "START_PROCESS"),
            new PMatch("DEFINITION", 5 /*DefinitionPermission.READ_STARTED_PROCESS*/, "READ_PROCESS"),
            new PMatch("DEFINITION", 6 /*DefinitionPermission.CANCEL_STARTED_PROCESS*/, "CANCEL_PROCESS"),

            new PMatch("PROCESS", 0 /*Permission.READ*/, "READ"),
            new PMatch("PROCESS", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("PROCESS", 2 /*ProcessPermission.CANCEL_PROCESS*/, "CANCEL_PROCESS"),

            new PMatch("RELATION", 0 /*Permission.READ*/, "READ"),
            new PMatch("RELATION", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("RELATION", 2 /*RelationPermission.UPDATE*/, "UPDATE_RELATION"),

            new PMatch("RELATIONGROUP", 0 /*Permission.READ*/, "READ"),
            new PMatch("RELATIONGROUP", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("RELATIONGROUP", 2 /*RelationPermission.UPDATE*/, "UPDATE_RELATION"),

            new PMatch("RELATIONPAIR", 0 /*Permission.READ*/, "READ"),
            new PMatch("RELATIONPAIR", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("RELATIONPAIR", 2 /*RelationPermission.UPDATE*/, "UPDATE_RELATION"),

            new PMatch("REPORT", 0 /*Permission.READ*/, "READ"),
            new PMatch("REPORT", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("REPORT", 2 /*ReportPermission.DEPLOY*/, "DEPLOY_REPORT"),

            new PMatch("SYSTEM", 0 /*Permission.READ*/, "READ"),
            new PMatch("SYSTEM", 1 /*Permission.UPDATE_PERMISSIONS*/, "UPDATE_PERMISSIONS"),
            new PMatch("SYSTEM", 2 /*SystemPermission.LOGIN_TO_SYSTEM*/, "LOGIN_TO_SYSTEM"),
            new PMatch("SYSTEM", 3 /*SystemPermission.CREATE_EXECUTOR*/, "CREATE_EXECUTOR"),
            new PMatch("SYSTEM", 5 /*SystemPermission.CHANGE_SELF_PASSWORD*/, "CHANGE_SELF_PASSWORD"),
            new PMatch("SYSTEM", 7 /*SystemPermission.VIEW_LOGS*/, "VIEW_LOGS"),
            new PMatch("SYSTEM", 4 /*WorkflowSystemPermission.DEPLOY_DEFINITION*/, "DEPLOY_DEFINITION"),
    };

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLRenameTable("permission_mapping", "permission_mapping__old"),
                getDDLCreateColumn("permission_mapping__old", new VarcharColumnDef("object_type", 32)),
                getDDLCreateColumn("permission_mapping__old", new VarcharColumnDef("permission", 32)),
                getDDLCreateTable(
                        "permission_mapping",
                        new ArrayList<ColumnDef>() {{
                            add(new BigintColumnDef("id").primaryKey());
                            add(new BigintColumnDef("executor_id").notNull());
                            add(new VarcharColumnDef("object_type", 32).notNull());
                            add(new BigintColumnDef("object_id").notNull());
                            add(new VarcharColumnDef("permission", 32).notNull());
                        }}
                ),
                getDDLCreateUniqueKey("permission_mapping", "uk_permission_mapping_4", "object_id", "object_type", "permission", "executor_id")
        );
    }

    @Override
    public void executeDML(Session session) throws Exception {

        // Fill `object_type` column in `permission_mapping__old`.
        {
            SQLQuery q = session.createSQLQuery("update permission_mapping__old set object_type=:s where type_id=:i");
            for (TMatch m : tMatches) {
                q.setParameter("s", m.name);
                q.setParameter("i", m.ordinal);
                q.executeUpdate();
            }

            @SuppressWarnings("unchecked")
            List<Object> rows = session
                    .createSQLQuery("select distinct type_id from permission_mapping__old where object_type is null order by type_id")
                    .list();
            if (!rows.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to convert type_id: ");
                boolean first = true;
                for (Object typeId : rows) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(typeId);
                }
                throw new Exception(sb.toString());
            }
        }

        // Fill `permission` column in `permission_mapping__old`.
        {
            SQLQuery q = session.createSQLQuery("update permission_mapping__old set permission=:p where object_type=:t and mask=:m");
            for (PMatch m : pMatches) {
                q.setParameter("p", m.name);
                q.setParameter("t", m.type);
                q.setParameter("m", 1 << m.maskPower);
                q.executeUpdate();
            }

            @SuppressWarnings("unchecked")
            List<Object[]> rows = session
                    .createSQLQuery("select distinct object_type, mask from permission_mapping__old where permission is null order by object_type, mask")
                    .list();
            if (!rows.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to convert (object_type,mask): ");
                boolean first = true;
                for (Object[] row : rows) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append("(").append(row[0]).append(",").append(row[1]).append(")");
                }
                throw new Exception(sb.toString());
            }
        }

        // Copy data to new table.
        {
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
            session.createSQLQuery(
                    "insert into permission_mapping (" + idName + "executor_id, object_type, object_id, permission) " +
                    "select " + idValue + "executor_id, object_type, identifiable_id, permission " +
                    "from permission_mapping__old"
            ).executeUpdate();
        }
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLDropTable("permission_mapping__old"),
                getDDLCreateIndex("permission_mapping", "ix_permission_mapping_data", "executor_id", "object_type", "permission", "object_id"),
                getDDLCreateForeignKey("permission_mapping", "fk_permission_executor", "executor_id", "executor", "id")
        );
    }
}
