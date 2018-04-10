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
package ru.runa.wfe.security;

import java.util.Collection;
import java.util.Collections;

import static ru.runa.wfe.security.Permission.ADD_TO_GROUP;
import static ru.runa.wfe.security.Permission.BOT_STATION_CONFIGURE;
import static ru.runa.wfe.security.Permission.CANCEL_PROCESS;
import static ru.runa.wfe.security.Permission.CHANGE_SELF_PASSWORD;
import static ru.runa.wfe.security.Permission.CREATE_EXECUTOR;
import static ru.runa.wfe.security.Permission.DEPLOY_DEFINITION;
import static ru.runa.wfe.security.Permission.DEPLOY_REPORT;
import static ru.runa.wfe.security.Permission.LIST_GROUP;
import static ru.runa.wfe.security.Permission.LOGIN_TO_SYSTEM;
import static ru.runa.wfe.security.Permission.READ;
import static ru.runa.wfe.security.Permission.READ_PROCESS;
import static ru.runa.wfe.security.Permission.REDEPLOY_DEFINITION;
import static ru.runa.wfe.security.Permission.REMOVE_FROM_GROUP;
import static ru.runa.wfe.security.Permission.START_PROCESS;
import static ru.runa.wfe.security.Permission.UNDEPLOY_DEFINITION;
import static ru.runa.wfe.security.Permission.UPDATE_ACTOR_STATUS;
import static ru.runa.wfe.security.Permission.UPDATE_EXECUTOR;
import static ru.runa.wfe.security.Permission.UPDATE_PERMISSIONS;
import static ru.runa.wfe.security.Permission.UPDATE_RELATION;
import static ru.runa.wfe.security.Permission.VIEW_ACTOR_TASKS;
import static ru.runa.wfe.security.Permission.VIEW_GROUP_TASKS;
import static ru.runa.wfe.security.Permission.VIEW_LOGS;

/**
 * Extracted from class Permission because permission-to-securedObjectType applicability is separate piece of logic,
 * orthogonal to both Permission and SecuredObjectType "extensible pseudo-enums" declarations.
 *
 * @see SecuredObjectType
 * @see Permission
 */
public class ApplicablePermissions {

    // Both list and set are unmodifiable.
    private static class ListAndSet {
        final java.util.List<Permission> list;
        final java.util.Set<Permission> set;

        ListAndSet(java.util.ArrayList<Permission> list, java.util.HashSet<Permission> set) {
            this.list = Collections.unmodifiableList(list);
            this.set = Collections.unmodifiableSet(set);
        }
    }

    // Mutable, but private. See accessors below.
    private static final java.util.HashMap<SecuredObjectType, ListAndSet> permissionsBySecuredObjectType = new java.util.HashMap<>();
    private static final java.util.List<Permission> emptyList = Collections.unmodifiableList(new java.util.ArrayList<Permission>());

    /**
     * Register permissions applicable to given SecuredObjectType. May be called multiple times for the same type;
     * each next call appends permissions to the list, excluding already listed permissions.
     */
    public static void add(SecuredObjectType type, Permission... permissions) {
        // Since ListAndSet is immutable, we fill temporary mutable collections and replace immutable instance.
        java.util.ArrayList<Permission> list = new java.util.ArrayList<>();
        java.util.HashSet<Permission> set = new java.util.HashSet<>();

        ListAndSet old = permissionsBySecuredObjectType.get(type);
        if (old != null) {
            list.addAll(old.list);
            set.addAll(old.set);
        }
        for (Permission p : permissions) {
            // This also excludes duplications in `permissions` argument itself, even if we created empty list just above.
            if (!set.contains(p)) {
                list.add(p);
                set.add(p);
            }
        }

        permissionsBySecuredObjectType.put(type, new ListAndSet(list, set));
    }

    /**
     * Returns permissions applicable to given SecuredObjectType. Returns unmodifiable list.
     * If no permissions were assigned to given SecuredObjectType, returns empty list.
     *
     * List with deterministic permission order is necessary for permission editor forms.
     */
    public static java.util.List<Permission> list(SecuredObjectType type) {
        // TODO After migrating to java 1.8, use getOrDefault().
//        return permissionsBySecuredObjectType.getOrDefault(type, emptyListAndSet).list;
        ListAndSet ls = permissionsBySecuredObjectType.get(type);
        return (ls != null) ? ls.list : emptyList;
    }

    /**
     * Shortcut for <code>list(obj.getSecuredObjectType())</code>, see {@link #list(SecuredObjectType)}.
     */
    public static java.util.List<Permission> list(SecuredObject obj) {
        return list(obj.getSecuredObjectType());
    }

    /**
     * Throws if permission is not applicable to secured object type.
     */
    public static void check(SecuredObjectType type, Permission permission) {
        ListAndSet ls = permissionsBySecuredObjectType.get(type);
        if (ls == null || !ls.set.contains(permission)) {
            throw new UnapplicablePermissionException(type, Collections.singletonList(permission));
        }
    }

    /**
     * Throws if permission is not applicable to secured object.
     */
    public static void check(SecuredObject securedObject, Permission permission) {
        check(securedObject.getSecuredObjectType(), permission);
    }

    /**
     * Throws if any of permissions is not applicable to secured object type.
     */
    public static void check(SecuredObjectType type, Collection<Permission> permissions) {
        if (permissions.isEmpty()) {
            return;
        }
        ListAndSet ls = permissionsBySecuredObjectType.get(type);
        if (ls == null) {
            throw new UnapplicablePermissionException(type, permissions);
        }
        // Used List instead of Set here, to have deterministic error message.
        java.util.List<Permission> unapplicable = ru.runa.wfe.commons.CollectionUtil.diffList(permissions, ls.set);
        if (unapplicable.size() > 0) {
            throw new UnapplicablePermissionException(type, unapplicable);
        }
    }

    /**
     * Throws if any of permissions is not applicable to secured object.
     */
    public static void check(SecuredObject obj, Collection<Permission> permissions) {
        check(obj.getSecuredObjectType(), permissions);
    }

    static {
        add(SecuredObjectType.ACTOR, READ, UPDATE_PERMISSIONS, UPDATE_EXECUTOR, UPDATE_ACTOR_STATUS, VIEW_ACTOR_TASKS);
        add(SecuredObjectType.GROUP, READ, UPDATE_PERMISSIONS, UPDATE_EXECUTOR, LIST_GROUP, ADD_TO_GROUP, REMOVE_FROM_GROUP, VIEW_GROUP_TASKS);
        add(SecuredObjectType.BOTSTATION, READ, UPDATE_PERMISSIONS, BOT_STATION_CONFIGURE);
        add(SecuredObjectType.DEFINITION, READ, UPDATE_PERMISSIONS, REDEPLOY_DEFINITION, UNDEPLOY_DEFINITION, START_PROCESS, READ_PROCESS, CANCEL_PROCESS);
        add(SecuredObjectType.PROCESS, READ, UPDATE_PERMISSIONS, CANCEL_PROCESS);
        add(SecuredObjectType.RELATION, READ, UPDATE_PERMISSIONS, UPDATE_RELATION);
        add(SecuredObjectType.RELATIONGROUP, READ, UPDATE_PERMISSIONS, UPDATE_RELATION);
        add(SecuredObjectType.RELATIONPAIR, READ, UPDATE_PERMISSIONS, UPDATE_RELATION);
        add(SecuredObjectType.REPORT, READ, UPDATE_PERMISSIONS, DEPLOY_REPORT);
        add(SecuredObjectType.SYSTEM, READ, UPDATE_PERMISSIONS, LOGIN_TO_SYSTEM, CREATE_EXECUTOR, CHANGE_SELF_PASSWORD, VIEW_LOGS, DEPLOY_DEFINITION);
    }
}
