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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;
import ru.runa.wfe.commons.CollectionUtil;

import static ru.runa.wfe.security.Permission.ALL;
import static ru.runa.wfe.security.Permission.CANCEL;
import static ru.runa.wfe.security.Permission.CANCEL_PROCESS;
import static ru.runa.wfe.security.Permission.CREATE_DEFINITION;
import static ru.runa.wfe.security.Permission.CREATE_EXECUTOR;
import static ru.runa.wfe.security.Permission.DELETE;
import static ru.runa.wfe.security.Permission.LIST;
import static ru.runa.wfe.security.Permission.LOGIN;
import static ru.runa.wfe.security.Permission.READ;
import static ru.runa.wfe.security.Permission.READ_LOGS;
import static ru.runa.wfe.security.Permission.READ_PERMISSIONS;
import static ru.runa.wfe.security.Permission.READ_PROCESS;
import static ru.runa.wfe.security.Permission.START;
import static ru.runa.wfe.security.Permission.UPDATE;
import static ru.runa.wfe.security.Permission.UPDATE_PERMISSIONS;
import static ru.runa.wfe.security.Permission.UPDATE_STATUS;
import static ru.runa.wfe.security.Permission.VIEW_TASKS;

/**
 * Extracted from class Permission because permission-to-securedObjectType applicability is separate piece of logic,
 * orthogonal to both Permission and SecuredObjectType "extensible pseudo-enums" declarations.
 *
 * @see SecuredObjectType
 * @see Permission
 * @see PermissionSubstitutions
 */
public final class ApplicablePermissions {

    // Both list and set are unmodifiable.
    private static final class ListAndSet {
        final ArrayList<Permission> visibleList = new ArrayList<>();
        final HashSet<Permission> allSet = new HashSet<>();
        final HashSet<Permission> defaultsSet = new HashSet<>();

        final List<Permission> unmodifiableVisibleList = Collections.unmodifiableList(visibleList);
    }

    // Mutable, but private. See accessors below.
    private static final HashMap<SecuredObjectType, ListAndSet> permissionsBySecuredObjectType = new HashMap<>();

    private static ListAndSet getOrCreate(SecuredObjectType type) {
        return CollectionUtil.mapGetOrPutDefault(permissionsBySecuredObjectType, type, new ListAndSet());
    }


    public static final class DSL {
        final SecuredObjectType type;

        private DSL(SecuredObjectType type) {
            this.type = type;
        }

        /**
         * Adds permissions that are invisible in permission editor form, but which are accepted by check() methods as applicable.
         * Hidden permissions are mapped to visible ones by PermissionSubstitutions class.
         * <p>
         * The reasoning: some logic may need to check LIST permission on arbitrary object. But it may be called for system object
         * which has only ALL permission applicable. To avoid polymorphic behaviour (choosing permission to check based on object type)
         * be stretched thin over whole project, this permission hiding and substitution is managed centrally: here and in the class
         * PermissionSubstitutions.
         * <p>
         * So, internally we have fine-grained permission system, while displaying coarse-grained permissions to users.
         */
        public DSL hidden(Permission... pp) {
            getOrCreate(type).allSet.addAll(Arrays.asList(pp));
            return this;
        }

        /**
         * Permissions granted by default when executor is added to managePermissionsForm by grantPermissionsForm.
         */
        public DSL defaults(Permission... pp) {
            List<Permission> ppList = Arrays.asList(pp);
            ListAndSet ls = getOrCreate(type);
            Set<Permission> unknown = CollectionUtil.diffSet(ppList, ls.visibleList);
            if (!unknown.isEmpty()) {
                throw new RuntimeException("For " + type + ", some default permissions are not visible: " + unknown);
            }
            ls.defaultsSet.addAll(ppList);
            return this;
        }
    }

    /**
     * Register permissions applicable to given SecuredObjectType and visible (i.e. editable by user; see also {@link DSL#hidden(Permission...)}).
     * May be called multiple times for the same type; each next call appends permissions to the list, except already listed permissions.
     * <p>
     * The order of visible permissions listed in the call to this method is important: it's the order they will show on permissions editor page.
     * Also, first visible permission will be granted to the executor by default when he is granted permissions in permission editor dialog.
     */
    public static DSL add(SecuredObjectType type, Permission... pp) {
        ListAndSet ls = getOrCreate(type);
        for (Permission p : pp) {
            // This also excludes duplications in `permissions` argument itself, even if we created empty list just above.
            if (!ls.allSet.contains(p)) {
                ls.allSet.add(p);
                ls.visibleList.add(p);
            }
        }
        return new DSL(type);
    }

    /**
     * Returns visible (editable by user) permissions applicable to given SecuredObjectType.
     * Returns non-empty unmodifiable list. Throws if no visible permissions were defined for type.
     * <p>
     * List with deterministic permission order is necessary for permission editor forms.
     */
    public static List<Permission> listVisible(SecuredObjectType type) {
        ListAndSet ls = permissionsBySecuredObjectType.get(type);
        Assert.notNull(ls);
        Assert.notEmpty(ls.unmodifiableVisibleList);
        return ls.unmodifiableVisibleList;
    }

    /**
     * Shortcut for <code>list(obj.getSecuredObjectType())</code>, see {@link #listVisible(SecuredObjectType)}.
     */
    public static List<Permission> listVisible(SecuredObject obj) {
        return listVisible(obj.getSecuredObjectType());
    }

    /**
     * Returns permissions granted by default, when user is added (granted permissions to object) on permissions editor page.
     * If getDefaults() was not specified for type, returns first element from listVisible(obj).
     */
    public static Set<Permission> getDefaults(SecuredObject obj) {
        ListAndSet ls = permissionsBySecuredObjectType.get(obj.getSecuredObjectType());
        return ls.defaultsSet.isEmpty()
                ? Collections.singleton(listVisible(obj).get(0))  // listVisible() has asserts, so called it instead of reading ls.listVisible.
                : ls.defaultsSet;
    }

    /**
     * Throws if permission is not applicable to secured object type, i.e. is not among neither visible nor hidden permissions.
     */
    public static void check(SecuredObjectType type, Permission permission) {
        ListAndSet ls = permissionsBySecuredObjectType.get(type);
        if (ls == null || !ls.allSet.contains(permission)) {
            throw new UnapplicablePermissionException(type, Collections.singletonList(permission));
        }
    }

    /**
     * Shortcut for <code>check(obj.getSecuredObjectType(), permission)</code>, see {@link #check(SecuredObjectType, Permission)}.
     */
    public static void check(SecuredObject obj, Permission permission) {
        check(obj.getSecuredObjectType(), permission);
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
        List<Permission> unapplicable = ru.runa.wfe.commons.CollectionUtil.diffList(permissions, ls.allSet);
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

    // List types in aplhabetic order, please. For each type:
    // - Visible permissions: add(type, ...) - in the order they'll appear in editor.
    // - Default permissions: .defaults(...) - in any order; optional: if omitted, first visible permission will be used.
    // - Hidden  permissions: .hidden(...)   - in any order; READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST (unless visible) must be present for all types.
    // ATTENTION!!! Lists of visible permissions are duplicated in RefactorPermissionsStep3 migration.
    static {

        // System singleton:
        add(SecuredObjectType.BOTSTATIONS, ALL)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST);

        add(SecuredObjectType.DEFINITION, ALL, LIST, READ, UPDATE, START, READ_PROCESS, CANCEL_PROCESS)
                .defaults(LIST)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS);

        add(SecuredObjectType.DEFINITIONS, ALL, LIST, READ, UPDATE, START, READ_PROCESS, CANCEL_PROCESS)
                .defaults(LIST)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS);

        // System singleton:
        add(SecuredObjectType.ERRORS, ALL)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST);

        add(SecuredObjectType.EXECUTOR, LIST, READ, VIEW_TASKS, UPDATE, UPDATE_STATUS, DELETE)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS);

        add(SecuredObjectType.PROCESS, ALL, LIST, READ, CANCEL)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS);

        // System singleton:
        add(SecuredObjectType.RELATIONS, ALL)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST);

        add(SecuredObjectType.REPORT, ALL, READ)
                .defaults(READ)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST);

        add(SecuredObjectType.REPORTS, ALL, READ)
                .defaults(READ)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST);

        // System singleton:
        add(SecuredObjectType.SYSTEM, ALL, LOGIN, CREATE_EXECUTOR, READ_LOGS, CREATE_DEFINITION)
                .defaults(LOGIN)
                .hidden(READ_PERMISSIONS, UPDATE_PERMISSIONS, LIST);
    }
}
