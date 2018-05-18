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

import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.HashMap;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.runa.wfe.commons.xml.Permission2XmlAdapter;


/**
 * "Extensible enum": more "enum items" can be added elsewhere.
 * For example, if subproject needs additional permissions EXTRA_EXECUTOR_PERM and EXTRA_ACTOR_PERM
 * applicable to actors and groups, code it like this:
 *
 * <pre>
 * class ExtraPermission {
 *     public static final Permission EXTRA_EXECUTOR_PERM = new Permission("EXTRA_EXECUTOR_PERM");
 *     public static final Permission EXTRA_ACTOR_PERM = new Permission("EXTRA_ACTOR_PERM");
 *     static {
 *         // Declare which types new permissions are applicable to.
 *         ApplicablePermissions.add(SecuredObjectType.ACTOR, EXTRA_EXECUTOR_PERM, EXTRA_ACTOR_PERM);
 *         ApplicablePermissions.add(SecuredObjectType.GROUP, EXTRA_EXECUTOR_PERM);
 *
 *         // Declare that if EXTRA_ACTOR_PERM is requested on ACTOR, then it's sufficient to have
 *         // UDPATE permission on the same ACTOR instance, or ALL or UPDATE permission on EXECUTORS list.
 *         // Note 1: declare substitutions after declaring applicability.
 *         // Note 2: list type EXECUTORS is known from SecuredObjectType.ACTOR definition.
 *         // Note 3: lists's ALL is already a substitution for list's UPDATE, and list's UPDATE is already
 *         //         a substitution for instance's UDPATE (see PermissionSubstitutions static initialization),
 *         //         so .list() call can be omitted here.
 *         PermissionSubstitutions.add(SecuredObjectType.ACTOR, EXTRA_ACTOR_PERM)
 *                 .self(Permission.UPDATE)
 *                 .list(Permission.ALL, Permission.UPDATE);
 *     }
 * }
 * </pre>
 *
 * These permissions are pretty fine-grained. Some of them are as hidden (globally or for specific object types),
 * i.e. used internally but not editable by user. See method {@link ApplicablePermissions.DSL#hidden(Permission...)}
 * for details.
 * <p>
 * <b>ATTENTION!!!</b> Since once initialization completes, permissions are accessed as read-only,
 * no synchronization is done on internal structures to avoid unnecessary performance overhead.
 * So you MUST initialize permissions in single thread. Make sure that all classes that perform
 * this initialization (Permission itself and, considering example above, ExtraPermission)
 * are touched by class-loader in main thread during application startup.
 *
 * @see SecuredObjectType
 * @see ApplicablePermissions
 * @see PermissionSubstitutions
 */
@XmlJavaTypeAdapter(Permission2XmlAdapter.class)
public final class Permission implements Serializable, Comparable<Permission> {
    private static final long serialVersionUID = 2L;

    private static HashMap<String, Permission> instancesByName = new HashMap<>();

    /**
     * Mimics enum's valueOf() method, including thrown exception type.
     *
     * Old Permission threw PermissionNotFoundException (now renamed to PermissionNotApplicableException)
     * which is subclass of InternalApplicationException; but unknown Throwable is wrapped
     * into InternalApplicationException by exception handlers, so there's no difference.
     */
    public static Permission valueOf(String name) {
        Permission result;
        try {
            result = instancesByName.get(name);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Illegal Permission name");
        }
        if (result == null) {
            throw new IllegalArgumentException("Unknown Permission name \"" + name + "\"");
        }
        return result;
    }


    private final String name;

    /**
     * @param name Should be equal to instance name. Returned by name() method.
     */
    public Permission(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) {
            // permission_mapping.permission is varchar(32)
            throw new RuntimeException("Null, empty or too large Permission name");
        }
        this.name = name;
        if (instancesByName.put(name, this) != null) {
            throw new RuntimeException("Duplicate Permission name \"" + name + "\"");
        }
    }

    /**
     * Equivalent to enum's name() method. Returns constructor argument.
     */
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Permission o) {
        return getName().compareTo(o.getName());
    }

    /**
     * Same as old Permission.toString().
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).toString();
    }


    /**
     * All permissions.
     */
    public static final Permission ALL = new Permission("ALL");

    /**
     * Cancel specific process or any processes.
     */
    public static final Permission CANCEL = new Permission("CANCEL");

    /**
     * Grant CANCEL permission for process started from specific definition or from any definitions.
     */
    public static final Permission CANCEL_PROCESS = new Permission("CANCEL_PROCESS");

    /**
     * Create or import object of specific type.
     */
    public static final Permission CREATE = new Permission("CREATE");

    /**
     * Delete specific object (when applied to that object), or any object of specific type (when applied to objects list).
     */
    public static final Permission DELETE = new Permission("DELETE");

    /**
     * Actor is allowed to login into system.
     */
    public static final Permission LOGIN = new Permission("LOGIN");

    /**
     * Formerly no-arg Permission constructor, used only as PropertyTDBuilder constructor argument in FieldDescriptor constructor calls.
     * PermissionDAO.isAllowed...() checks always return false for it, without accessing database.
     *
     * TODO Review all usages. PropertyTDBuilder's 2/3 constructors' behaviour looks contradictionary with its own base class.
     *      Maybe with a little more refactoring, NONE can be removed and null can be passed everywhere instead.
     */
    public static final Permission NONE = new Permission("NONE");

    /**
     * Read objects list, corresponding menu item is visible.
     * When applied to specific objects instead of whole list, only those objects will be visible in list.
     */
    public static final Permission LIST = new Permission("LIST");

    /**
     * Can read and export any object details.
     */
    public static final Permission READ = new Permission("READ");

    /**
     * Can read object permissions.
     */
    public static final Permission READ_PERMISSIONS = new Permission("READ_PERMISSIONS");

    /**
     * Grant READ permission for process started from specific definition or from any definitions.
     */
    public static final Permission READ_PROCESS = new Permission("READ_PROCESS");

    /**
     * Can start specific process (when applied to process) or any processes (when applied to process list).
     */
    public static final Permission START = new Permission("START");

    /**
     * Can edit specific object (when applied to that object) or any object of specific type (when applied to objects list).
     */
    public static final Permission UPDATE = new Permission("UPDATE");

    /**
     * Can edit object permissions.
     */
    public static final Permission UPDATE_PERMISSIONS = new Permission("UPDATE_PERMISSIONS");

    /**
     * Actor can edit his own profile.
     */
    public static final Permission UPDATE_SELF = new Permission("UPDATE_SELF");

    /**
     * Can edit actors's status.
     */
    public static final Permission UPDATE_STATUS = new Permission("UPDATE_STATUS");

    /**
     * Can view actor's tasks.
     */
    public static final Permission VIEW_TASKS = new Permission("VIEW_TASKS");
}
