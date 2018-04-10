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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.google.common.base.Objects;
import ru.runa.wfe.commons.xml.Permission2XmlAdapter;


/**
 * "Extensible enum": more "enum items" can be added elsewhere.
 * For example, if subproject needs additional permissions EXTRA_EXECUTOR_PERM and EXTRA_ACTOR_PERM
 * applicable to executors and actors respectively, code it like this:
 *
 * <pre>
 * class ExtraPermission {
 *     public static final Permission EXTRA_EXECUTOR_PERM = new Permission("EXTRA_EXECUTOR_PERM");
 *     public static final Permission EXTRA_ACTOR_PERM = new Permission("EXTRA_ACTOR_PERM");
 *     static {
 *         ApplicablePermissions.add(SecuredObjectType.ACTOR, EXTRA_EXECUTOR_PERM, EXTRA_ACTOR_PERM);
 *         ApplicablePermissions.add(SecuredObjectType.GROUP, EXTRA_EXECUTOR_PERM);
 *     }
 * }
 * </pre>
 *
 * <b>ATTENTION!!!</b> Since once initialization completes, permissions are accessed as read-only,
 * no synchronization is done on internal structures to avoid unnecessary performance overhead.
 * So you MUST initialize permissions in single thread. Make sure that all classes that perform
 * this initialization (Permission itself and, considering example above, ExtraPermission)
 * are touched by class-loader in main thread during application startup.
 *
 * @see SecuredObjectType
 * @see ApplicablePermissions
 * @see LegacyPermissions
 */
@XmlJavaTypeAdapter(Permission2XmlAdapter.class)
public final class Permission implements Serializable {

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
            // Accept "permission.xxx" in addition to "XXX".
            // TODO This is temporary hack, will do until we start renaiming secured object types and permissions. May stay for a while,
            //      because script execuion infrastructure requires deep refactoring before LegacyPermissions can be used by it.
            if (name != null && name.startsWith("permission.")) {
                result = instancesByName.get(name.substring(11).toUpperCase());
            }

            if (result == null) {
                throw new IllegalArgumentException("Unknown Permission name \"" + name + "\"");
            }
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

    /**
     * Same as old Permission.toString().
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).toString();
    }


    /**
     * Formerly no-arg Permission constructor, used only as PropertyTDBuilder constructor argument in FieldDescriptor constructor calls.
     * PermissionDAO.isAllowed...() checks always return false for it, without accessing database.
     *
     * TODO Review all usages. PropertyTDBuilder's 2/3 constructors' behaviour looks contradictionary with its own base class.
     *      Maybe with a little more refactoring, NO_PERMISSION can be removed and null can be passed everywhere instead.
     */
    public static final Permission NO_PERMISSION = new Permission("NO_PERMISSION");

    // Former Permission:
    public static final Permission READ = new Permission("READ");
    public static final Permission UPDATE_PERMISSIONS = new Permission("UPDATE_PERMISSIONS");

    // Former ExecutorPermission:
    public static final Permission UPDATE_EXECUTOR = new Permission("UPDATE_EXECUTOR");

    // Former ActorPermission:
    public static final Permission UPDATE_ACTOR_STATUS = new Permission("UPDATE_ACTOR_STATUS");
    public static final Permission VIEW_ACTOR_TASKS = new Permission("VIEW_ACTOR_TASKS");

    // Former GroupPermission:
    public static final Permission LIST_GROUP = new Permission("LIST_GROUP");
    public static final Permission ADD_TO_GROUP = new Permission("ADD_TO_GROUP");
    public static final Permission REMOVE_FROM_GROUP = new Permission("REMOVE_FROM_GROUP");
    public static final Permission VIEW_GROUP_TASKS = new Permission("VIEW_GROUP_TASKS");

    // Former BotStationPermission:
    public static final Permission BOT_STATION_CONFIGURE = new Permission("BOT_STATION_CONFIGURE");

    // Former DefinitionPermission:
    public static final Permission REDEPLOY_DEFINITION = new Permission("REDEPLOY_DEFINITION");
    public static final Permission UNDEPLOY_DEFINITION = new Permission("UNDEPLOY_DEFINITION");
    public static final Permission START_PROCESS = new Permission("START_PROCESS");
    public static final Permission READ_PROCESS = new Permission("READ_PROCESS");

    // Former DefinitionPermission and ProcessPermission:
    public static final Permission CANCEL_PROCESS = new Permission("CANCEL_PROCESS");

    // Former RelationPermission:
    public static final Permission UPDATE_RELATION = new Permission("UPDATE_RELATION");

    // Former ReportPermission:
    public static final Permission DEPLOY_REPORT = new Permission("DEPLOY_REPORT");

    // Former SystemPermission:
    public static final Permission LOGIN_TO_SYSTEM = new Permission("LOGIN_TO_SYSTEM");
    public static final Permission CREATE_EXECUTOR = new Permission("CREATE_EXECUTOR");
    public static final Permission CHANGE_SELF_PASSWORD = new Permission("CHANGE_SELF_PASSWORD");
    public static final Permission VIEW_LOGS = new Permission("VIEW_LOGS");

    // Former WorkflowSystemPermission:
    public static final Permission DEPLOY_DEFINITION = new Permission("DEPLOY_DEFINITION");


    /**
     * Frequently used shortcut: unmodifiable list containing single READ element.
     */
    public static final List<Permission> readPermissions = Collections.unmodifiableList(Collections.singletonList(READ));
}
