package ru.runa.wfe.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.runa.wfe.commons.CollectionUtil;

import static ru.runa.wfe.security.Permission.ALL;
import static ru.runa.wfe.security.Permission.CANCEL;
import static ru.runa.wfe.security.Permission.CANCEL_PROCESS;
import static ru.runa.wfe.security.Permission.DELETE;
import static ru.runa.wfe.security.Permission.READ;
import static ru.runa.wfe.security.Permission.READ_PERMISSIONS;
import static ru.runa.wfe.security.Permission.READ_PROCESS;
import static ru.runa.wfe.security.Permission.START;
import static ru.runa.wfe.security.Permission.UPDATE;
import static ru.runa.wfe.security.Permission.UPDATE_PERMISSIONS;
import static ru.runa.wfe.security.Permission.UPDATE_STATUS;
import static ru.runa.wfe.security.SecuredObjectType.BOTSTATIONS;
import static ru.runa.wfe.security.SecuredObjectType.DEFINITION;
import static ru.runa.wfe.security.SecuredObjectType.EXECUTOR;
import static ru.runa.wfe.security.SecuredObjectType.PROCESS;
import static ru.runa.wfe.security.SecuredObjectType.RELATION;
import static ru.runa.wfe.security.SecuredObjectType.RELATIONS;
import static ru.runa.wfe.security.SecuredObjectType.REPORT;
import static ru.runa.wfe.security.SecuredObjectType.REPORTS;
import static ru.runa.wfe.security.SecuredObjectType.SYSTEM;

/**
 * A registry of permission substitutions. For example, if UPDATE_STATUS permission is checked on specific ACTOR, access should be granted
 * if any of next permissions are granted:
 *
 * <ul>
 *     <li>UPDATE_STATUS on given ACTOR (as requested);
 *     <li>UPDATE on given ACTOR;
 *     <li>UPDATE_STATUS on all EXECUTORS;
 *     <li>UPDATE on all EXECUTORS;
 *     <li>ALL on all EXECUTORS.
 * </ul>
 *
 * Substitution relations are transitive; e.g. if ALL on EXECUTORS assumes UPDATE on EXECUTORS which in turn assumes UPDATE on any ACTOR,
 * then ALL on EXECUTORS assumes UPDATE on any ACTOR.
 * <p>
 * Used by PermissionDAO.isAllowed() methods. Also may be used by permission editor forms: e.g. if admin checks ALL permission on EXECUTORS,
 * then UPDATE and UPDATE_STATUS checkboxes should be disabled.
 *
 * @see SecuredObjectType
 * @see Permission
 * @see ApplicablePermissions
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class PermissionSubstitutions {

    /**
     * Immutable. Needed to check permissions.
     * <p>
     * Note about name "ForCheck": there also has been ForForm logic for disabling permission checkboxes superseded by checked permissions,
     * but incomplete and dropped; maybe I'll get back to it later.
     */
    public static final class ForCheck {
        private final HashSet<Permission> mutableSelfPermissions = new HashSet<>();
        private final HashSet<Permission> mutableListPermissions = new HashSet<>();
        public final Set<Permission> selfPermissions = Collections.unmodifiableSet(mutableSelfPermissions);
        public final Set<Permission> listPermissions = Collections.unmodifiableSet(mutableListPermissions);

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ForCheck forCheck = (ForCheck) o;
            return Objects.equals(mutableSelfPermissions, forCheck.mutableSelfPermissions) &&
                    Objects.equals(mutableListPermissions, forCheck.mutableListPermissions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mutableSelfPermissions, mutableListPermissions);
        }
    }

    private static class Key {
        final SecuredObjectType type;
        final Permission permission;

        Key(SecuredObjectType t, Permission p) {
            type = t;
            permission = p;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(type, key.type) && Objects.equals(permission, key.permission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, permission);
        }
    }

    private static HashMap<Key, ForCheck> forCheck = new HashMap<>();

    private static ForCheck getOrCreateForCheck(Key key) {
        return CollectionUtil.mapGetOrPutDefault(forCheck, key, new ForCheck());
    }

    private static boolean addSelfPermission(Key key, Permission p) {
        ForCheck fc = getOrCreateForCheck(key);
        if (fc.mutableSelfPermissions.contains(p)) {
            return false;
        }
        fc.mutableSelfPermissions.add(p);
        // If key.permission is superseded by p, then it's also superseded by all permissions by which p itself is superseded.
        ForCheck fc2 = forCheck.get(new Key(key.type, p));
        if (fc2 != null) {
            fc.mutableSelfPermissions.addAll(fc2.mutableSelfPermissions);
            fc.mutableListPermissions.addAll(fc2.mutableListPermissions);
        }
        return true;
    }

    private static boolean addListPermission(Key key, Permission p) {
        ForCheck fc = getOrCreateForCheck(key);
        if (!fc.mutableListPermissions.add(p)) {
            return false;
        }

        // Add transitive substitutions.
        ForCheck fc2 = forCheck.get(new Key(key.type.getListType(), p));
        if (fc2 != null) {
            fc.mutableListPermissions.addAll(fc2.selfPermissions);
        }

        return true;
    }

    /**
     * Propagates changes to dependents, for the case if substitutions were specified in wrong order.
     */
    private static void updateDependents(Key key) {
        for (Map.Entry<Key, ForCheck> kv : forCheck.entrySet()) {
            Key key2 = kv.getKey();
            ForCheck r2 = kv.getValue();

            if (key2 == key) {
                continue;
            }

            if (key2.type == key.type && r2.selfPermissions.contains(key.permission)) {
                ForCheck r = forCheck.get(key);
                for (Permission p : r.selfPermissions) {
                    addSelfPermission(key2, p);
                }
                for (Permission p : r.mutableListPermissions) {
                    addListPermission(key2, p);
                }
            } else if (key2.type.getListType() == key.type && r2.mutableListPermissions.contains(key.permission)) {
                ForCheck r = forCheck.get(key);
                for (Permission p : r.selfPermissions) {
                    addListPermission(key2, p);
                }
            }
        }
    }


    /**
     * Adds permission whitout substitutions. Use methods self() and list() on returned DSL object to define substitutions.
     */
    public static DSL add(SecuredObjectType t, Permission p) {
        ApplicablePermissions.check(t, p);
        return new DSL(new Key(t, p));
    }

    public static final class DSL {
        private final Key key;

        private DSL(Key key) {
            this.key = key;
            addSelfPermission(key, key.permission);
            // No need to call updateDependents() here.
        }

        /**
         * Registers permissions on the same object type which assume (include, can be checked instead of) permission specified in add().
         */
        @SuppressWarnings("UnusedReturnValue")
        DSL self(Permission... pp) {
            boolean added = false;
            for (Permission p : pp) {
                ApplicablePermissions.check(key.type, p);
                if (addSelfPermission(key, p)) {
                    added = true;
                }
            }
            if (added) {
                updateDependents(key);
            }
            return this;
        }

        /**
         * Registers permissions on object's list type which assume (include, can be checked instead of) permission specified in add().
         * <p>
         * If no parameters are given, same permission as passsed to add() is used;
         * e.g. <code>add(ACTOR, LIST).list()</code> is equal to <code>add(ACTOR, LIST).list(LIST)</code>.
         * <p>
         * Currently <code>list()</code> is always called with empty parameter list, but I kept it just in case
         * for possible future extensions; it's symmetric with self() and does not introduce complexity.
         */
        @SuppressWarnings("UnusedReturnValue")
        DSL list(Permission... pp) {
            SecuredObjectType listType = key.type.getListType();
            if (listType == null) {
                throw new RuntimeException("listType == null for type " + key.type);
            }
            if (pp.length == 0) {
                pp = new Permission[] { key.permission };
            }
            boolean added = false;
            for (Permission p : pp) {
                ApplicablePermissions.check(listType, p);
                if (addListPermission(key, p)) {
                    added = true;
                }
            }
            if (added) {
                updateDependents(key);
            }
            return this;
        }
    }


    /**
     * Never returns null. Returned response contains non-empty selfPermissions which contains at least p.
     */
    public static ForCheck getForCheck(SecuredObjectType t, Permission p) {
        ForCheck r = forCheck.get(new Key(t, p));
        if (r == null) {
            // I do not cache this instance, because that would require synchronization.
            r = new ForCheck();
            r.mutableSelfPermissions.add(p);
        }
        return r;
    }


    // In alphabetic order, please:
    static {

        // System singleton:
        add(BOTSTATIONS, READ).self(UPDATE);

        add(DEFINITION, CANCEL_PROCESS).self(ALL);
        add(DEFINITION, ALL);
        add(DEFINITION, READ).self(ALL, START, UPDATE);
        add(DEFINITION, READ_PERMISSIONS).self(READ, UPDATE_PERMISSIONS);
        add(DEFINITION, READ_PROCESS).self(ALL);
        add(DEFINITION, START).self(ALL);
        add(DEFINITION, UPDATE).self(ALL);
        add(DEFINITION, UPDATE_PERMISSIONS).self(ALL, UPDATE);

        add(EXECUTOR, READ).self(UPDATE, UPDATE_STATUS, DELETE);
        add(EXECUTOR, READ_PERMISSIONS).self(READ, UPDATE_PERMISSIONS);
        add(EXECUTOR, UPDATE);
        add(EXECUTOR, UPDATE_STATUS).self(UPDATE);
        add(EXECUTOR, UPDATE_PERMISSIONS).self(UPDATE);

        add(PROCESS, ALL);
        add(PROCESS, CANCEL).self(ALL);
        add(PROCESS, READ).self(ALL, READ, CANCEL);
        add(PROCESS, READ_PERMISSIONS).self(READ, UPDATE_PERMISSIONS);
        add(PROCESS, UPDATE_PERMISSIONS).self(ALL);

//        add(RELATION, UPDATE).list();
//        add(RELATION, READ).self(UPDATE).list();
        add(RELATION, READ).self(UPDATE);

        add(RELATIONS, READ).self(UPDATE);

//        add(REPORT, UPDATE).list();
//        add(REPORT, READ).self(UPDATE).list();
        add(REPORT, READ).self(UPDATE);

        add(REPORTS, READ).self(UPDATE);

        // System singleton:
        add(SYSTEM, READ_PERMISSIONS).self(READ, UPDATE_PERMISSIONS);
        add(SYSTEM, UPDATE_PERMISSIONS).self(READ);
    }
}
