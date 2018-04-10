package ru.runa.wfe.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Compatibility layer to support old scripts and web-service requests.
 *
 * Translates old (legacy) secured object type names + permission names to new ones.
 * Translation MUST BE NARROWING, so either same or less permissions are given (none if there's no match at all), but not more.
 *
 * Should be called BEFORE validating against ApplicablePermissions, because secured object types are also changed
 * incompatibly: e.g. former ("REPORT", "permission.read") soon shall mean ("REPORTS", "READ") but "REPORT" shall mean
 * single report, not all reports.
 *
 * TODO Incomplete, unused. Script processing infrastructure requires deep refactoring before this class can be used by it.
 *
 * @see SecuredObjectType
 * @see Permission
 * @see ApplicablePermissions
 */
public final class LegacyPermissions {

    private static final class Old {
        final String securedObjectTypeName;
        final String permissionName;

        Old(String securedObjectTypeName, String permissionName) {
            this.securedObjectTypeName = securedObjectTypeName;
            this.permissionName = permissionName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Old old = (Old) o;
            return Objects.equals(securedObjectTypeName, old.securedObjectTypeName) &&
                    Objects.equals(permissionName, old.permissionName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(securedObjectTypeName, permissionName);
        }
    }

    public static final class Replacement {
        /** Not null. */
        public final SecuredObjectType securedObjectType;

        /** Not null. */
        public final Permission permission;

        private Replacement(SecuredObjectType securedObjectType, Permission permission) {
            this.securedObjectType = securedObjectType;
            this.permission = permission;
        }
    }

    private static Map<Old, Replacement> replacements = new HashMap<Old, Replacement>() {{
        put(new Old("ACTOR", "permission.read"), new Replacement(SecuredObjectType.ACTOR, Permission.READ));
        put(new Old("ACTOR", "permission.update_permissions"), new Replacement(SecuredObjectType.ACTOR, Permission.UPDATE_PERMISSIONS));
        put(new Old("ACTOR", "permission.update_executor"), new Replacement(SecuredObjectType.ACTOR, Permission.UPDATE_EXECUTOR));
        put(new Old("ACTOR", "permission.update_actor_status"), new Replacement(SecuredObjectType.ACTOR, Permission.UPDATE_ACTOR_STATUS));
        put(new Old("ACTOR", "permission.view_actor_tasks"), new Replacement(SecuredObjectType.ACTOR, Permission.VIEW_ACTOR_TASKS));

        put(new Old("GROUP", "permission.read"), new Replacement(SecuredObjectType.GROUP, Permission.READ));
        put(new Old("GROUP", "permission.update_permissions"), new Replacement(SecuredObjectType.GROUP, Permission.UPDATE_PERMISSIONS));
        put(new Old("GROUP", "permission.update_executor"), new Replacement(SecuredObjectType.GROUP, Permission.UPDATE_EXECUTOR));
        put(new Old("GROUP", "permission.list_group"), new Replacement(SecuredObjectType.GROUP, Permission.LIST_GROUP));
        put(new Old("GROUP", "permission.add_to_group"), new Replacement(SecuredObjectType.GROUP, Permission.ADD_TO_GROUP));
        put(new Old("GROUP", "permission.remove_from_group"), new Replacement(SecuredObjectType.GROUP, Permission.REMOVE_FROM_GROUP));
        put(new Old("GROUP", "permission.view_group_tasks"), new Replacement(SecuredObjectType.GROUP, Permission.VIEW_GROUP_TASKS));

        put(new Old("BOTSTATION", "permission.read"), new Replacement(SecuredObjectType.BOTSTATION, Permission.READ));
        put(new Old("BOTSTATION", "permission.update_permissions"), new Replacement(SecuredObjectType.BOTSTATION, Permission.UPDATE_PERMISSIONS));
        put(new Old("BOTSTATION", "permission.bot_station_configure"), new Replacement(SecuredObjectType.BOTSTATION, Permission.BOT_STATION_CONFIGURE));

        put(new Old("DEFINITION", "permission.read"), new Replacement(SecuredObjectType.DEFINITION, Permission.READ));
        put(new Old("DEFINITION", "permission.update_permissions"), new Replacement(SecuredObjectType.DEFINITION, Permission.UPDATE_PERMISSIONS));
        put(new Old("DEFINITION", "permission.redeploy_definition"), new Replacement(SecuredObjectType.DEFINITION, Permission.REDEPLOY_DEFINITION));
        put(new Old("DEFINITION", "permission.undeploy_definition"), new Replacement(SecuredObjectType.DEFINITION, Permission.UNDEPLOY_DEFINITION));
        put(new Old("DEFINITION", "permission.start_process"), new Replacement(SecuredObjectType.DEFINITION, Permission.START_PROCESS));
        put(new Old("DEFINITION", "permission.read_process"), new Replacement(SecuredObjectType.DEFINITION, Permission.READ_PROCESS));
        put(new Old("DEFINITION", "permission.cancel_process"), new Replacement(SecuredObjectType.DEFINITION, Permission.CANCEL_PROCESS));

        put(new Old("PROCESS", "permission.read"), new Replacement(SecuredObjectType.PROCESS, Permission.READ));
        put(new Old("PROCESS", "permission.update_permissions"), new Replacement(SecuredObjectType.PROCESS, Permission.UPDATE_PERMISSIONS));
        put(new Old("PROCESS", "permission.cancel_process"), new Replacement(SecuredObjectType.PROCESS, Permission.CANCEL_PROCESS));

        put(new Old("RELATION", "permission.read"), new Replacement(SecuredObjectType.RELATION, Permission.READ));
        put(new Old("RELATION", "permission.update_permissions"), new Replacement(SecuredObjectType.RELATION, Permission.UPDATE_PERMISSIONS));
        put(new Old("RELATION", "permission.update_relation"), new Replacement(SecuredObjectType.RELATION, Permission.UPDATE_RELATION));

        put(new Old("REPORT", "permission.read"), new Replacement(SecuredObjectType.REPORT, Permission.READ));
        put(new Old("REPORT", "permission.update_permissions"), new Replacement(SecuredObjectType.REPORT, Permission.UPDATE_PERMISSIONS));
        put(new Old("REPORT", "permission.deploy_report"), new Replacement(SecuredObjectType.REPORT, Permission.DEPLOY_REPORT));

        put(new Old("SYSTEM", "permission.read"), new Replacement(SecuredObjectType.SYSTEM, Permission.READ));
        put(new Old("SYSTEM", "permission.update_permissions"), new Replacement(SecuredObjectType.SYSTEM, Permission.UPDATE_PERMISSIONS));
        put(new Old("SYSTEM", "permission.login_to_system"), new Replacement(SecuredObjectType.SYSTEM, Permission.LOGIN_TO_SYSTEM));
        put(new Old("SYSTEM", "permission.create_executor"), new Replacement(SecuredObjectType.SYSTEM, Permission.CREATE_EXECUTOR));
        put(new Old("SYSTEM", "permission.change_self_password"), new Replacement(SecuredObjectType.SYSTEM, Permission.CHANGE_SELF_PASSWORD));
        put(new Old("SYSTEM", "permission.view_logs"), new Replacement(SecuredObjectType.SYSTEM, Permission.VIEW_LOGS));
        put(new Old("SYSTEM", "permission.deploy_definition"), new Replacement(SecuredObjectType.SYSTEM, Permission.DEPLOY_DEFINITION));
    }};

    /**
     * @param oldSecuredObjectTypeName  Must be non-empty, i.e. must specify concrete secured object type, "for any" wildcards are not allowed.
     * @param oldPermissionName  Must be non-empty, i.e. must specify concrete permission.
     * @return  Null if not found.
     */
    public static Replacement findReplacement(String oldSecuredObjectTypeName, String oldPermissionName) {
        return replacements.get(new Old(oldSecuredObjectTypeName, oldPermissionName));
    }
}
