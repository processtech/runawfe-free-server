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
import static ru.runa.wfe.security.Permission.CREATE;
import static ru.runa.wfe.security.Permission.DELETE;
import static ru.runa.wfe.security.Permission.LIST;
import static ru.runa.wfe.security.Permission.LOGIN;
import static ru.runa.wfe.security.Permission.READ;
import static ru.runa.wfe.security.Permission.READ_PERMISSIONS;
import static ru.runa.wfe.security.Permission.READ_PROCESS;
import static ru.runa.wfe.security.Permission.START;
import static ru.runa.wfe.security.Permission.UPDATE;
import static ru.runa.wfe.security.Permission.UPDATE_PERMISSIONS;
import static ru.runa.wfe.security.Permission.UPDATE_SELF;
import static ru.runa.wfe.security.Permission.UPDATE_STATUS;
import static ru.runa.wfe.security.Permission.VIEW_TASKS;
import static ru.runa.wfe.security.SecuredObjectType.BOTSTATIONS;
import static ru.runa.wfe.security.SecuredObjectType.DATAFILE;
import static ru.runa.wfe.security.SecuredObjectType.DEFINITION;
import static ru.runa.wfe.security.SecuredObjectType.DEFINITIONS;
import static ru.runa.wfe.security.SecuredObjectType.ERRORS;
import static ru.runa.wfe.security.SecuredObjectType.EXECUTORS;
import static ru.runa.wfe.security.SecuredObjectType.EXECUTOR;
import static ru.runa.wfe.security.SecuredObjectType.LOGS;
import static ru.runa.wfe.security.SecuredObjectType.PROCESS;
import static ru.runa.wfe.security.SecuredObjectType.PROCESSES;
import static ru.runa.wfe.security.SecuredObjectType.RELATIONS;
import static ru.runa.wfe.security.SecuredObjectType.REPORT;
import static ru.runa.wfe.security.SecuredObjectType.REPORTS;
import static ru.runa.wfe.security.SecuredObjectType.SCRIPTS;
import static ru.runa.wfe.security.SecuredObjectType.SUBSTITUTION_CRITERIAS;
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
        add(BOTSTATIONS, LIST).self(ALL);
        add(BOTSTATIONS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(BOTSTATIONS, UPDATE_PERMISSIONS).self(ALL);

        // System singleton:
        add(DATAFILE, LIST).self(ALL);
        add(DATAFILE, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(DATAFILE, UPDATE_PERMISSIONS).self(ALL);

        add(DEFINITION, CANCEL_PROCESS).self(ALL).list();
        add(DEFINITION, ALL).list();
        add(DEFINITION, LIST).self(ALL, READ, START, UPDATE).list();
        add(DEFINITION, READ).self(ALL, UPDATE).list();
        add(DEFINITION, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS, READ).list();
        add(DEFINITION, READ_PROCESS).self(ALL).list();
        add(DEFINITION, START).self(ALL).list();
        add(DEFINITION, UPDATE).self(ALL).list();
        add(DEFINITION, UPDATE_PERMISSIONS).self(ALL, UPDATE).list();

        add(DEFINITIONS, CANCEL_PROCESS).self(ALL);
        add(DEFINITIONS, CREATE).self(ALL);
        add(DEFINITIONS, LIST).self(ALL, READ, START, CREATE, UPDATE);
        add(DEFINITIONS, READ).self(ALL, UPDATE);
        add(DEFINITIONS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS, READ);
        add(DEFINITIONS, READ_PROCESS).self(ALL);
        add(DEFINITIONS, START).self(ALL);
        add(DEFINITIONS, UPDATE).self(ALL);
        add(DEFINITIONS, UPDATE_PERMISSIONS).self(ALL, UPDATE);

        // System singleton:
        add(ERRORS, LIST).self(ALL);
        add(ERRORS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(ERRORS, UPDATE_PERMISSIONS).self(ALL);

        add(EXECUTOR, DELETE).list();
        add(EXECUTOR, LIST).self(READ, UPDATE_STATUS, DELETE).list();
        add(EXECUTOR, READ).self(UPDATE).list();
        add(EXECUTOR, READ_PERMISSIONS).self(UPDATE_PERMISSIONS, READ).list();
        add(EXECUTOR, UPDATE).list();
        add(EXECUTOR, UPDATE_STATUS).self(UPDATE).list();
        add(EXECUTOR, UPDATE_PERMISSIONS).self(UPDATE).list();

        add(EXECUTORS, CREATE).self(ALL);
        add(EXECUTORS, DELETE).self(ALL);
        add(EXECUTORS, LOGIN).self(ALL);
        add(EXECUTORS, LIST).self(ALL, READ, UPDATE_STATUS, DELETE);
        add(EXECUTORS, READ).self(ALL, UPDATE);
        add(EXECUTORS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS, READ);
        add(EXECUTORS, VIEW_TASKS).self(ALL);
        add(EXECUTORS, UPDATE).self(ALL);
        add(EXECUTORS, UPDATE_PERMISSIONS).self(ALL, UPDATE);
        add(EXECUTORS, UPDATE_SELF).self(ALL, UPDATE);
        add(EXECUTORS, UPDATE_STATUS).self(ALL, UPDATE);

        // System singleton:
        add(LOGS, LIST).self(ALL);
        add(LOGS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(LOGS, UPDATE_PERMISSIONS).self(ALL);

        add(PROCESS, ALL).list();
        add(PROCESS, CANCEL).self(ALL).list();
        add(PROCESS, LIST).self(ALL, READ, CANCEL).list();
        add(PROCESS, READ).self(ALL).list();
        add(PROCESS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS).list();
        add(PROCESS, UPDATE_PERMISSIONS).self(ALL).list();

        add(PROCESSES, CANCEL).self(ALL);
        add(PROCESSES, LIST).self(ALL, READ, CANCEL);
        add(PROCESSES, READ).self(ALL);
        add(PROCESSES, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(PROCESSES, UPDATE_PERMISSIONS).self(ALL);

        // System singleton:
        add(RELATIONS, LIST).self(ALL);
        add(RELATIONS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(RELATIONS, UPDATE_PERMISSIONS).self(ALL);

        add(REPORT, ALL).list();
        add(REPORT, READ).self(ALL).list();
        add(REPORT, LIST).self(READ).list();
        add(REPORT, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS).list();
        add(REPORT, UPDATE_PERMISSIONS).self(ALL).list();

        add(REPORTS, READ).self(ALL);
        add(REPORTS, LIST).self(READ);
        add(REPORTS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(REPORTS, UPDATE_PERMISSIONS).self(ALL);

        // System singleton:
        add(SCRIPTS, LIST).self(ALL);
        add(SCRIPTS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(SCRIPTS, UPDATE_PERMISSIONS).self(ALL);

        // System singleton:
        add(SUBSTITUTION_CRITERIAS, LIST).self(ALL);
        add(SUBSTITUTION_CRITERIAS, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(SUBSTITUTION_CRITERIAS, UPDATE_PERMISSIONS).self(ALL);

        // System singleton:
        add(SYSTEM, LIST).self(ALL);
        add(SYSTEM, READ_PERMISSIONS).self(ALL, UPDATE_PERMISSIONS);
        add(SYSTEM, UPDATE_PERMISSIONS).self(ALL);
    }
}
