package ru.runa.wfe.presentation.hibernate;

import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.User;

/**
 * Restrictions to load only objects with specified permission granted for user.
 */
public class RestrictionsToPermissions {
    /**
     * User (with groups) which must has permission on queried objects. Queries only objects with condition: at least one executor from executorIds
     * (user + its groups) must have 'permission' with 'securedObjectTypes'. Can be null.
     */
    private final List<Long> executorIdsToCheckPermission;

    /**
     * Permission, which at least one executors must has on queried objects. Queries only objects with condition: at least one executor from
     * executorIds must have 'permission' with 'securedObjectTypes'
     */
    private final Permission permission;

    /**
     * Type of secured object for queried objects. Queries only objects with condition: at least one executor from executorIds must have 'permission'
     * with 'securedObjectTypes'
     */
    private final SecuredObjectType[] securedObjectTypes;

    public RestrictionsToPermissions(User user, Permission permission, SecuredObjectType[] securedObjectTypes) {
        super();
        if (user == null || permission == null || securedObjectTypes == null) {
            throw new InternalApplicationException("Can't build query with permission check. No secured parametes specified.");
        }
        this.permission = permission;
        this.securedObjectTypes = securedObjectTypes;
        List<Long> executorIds = ApplicationContextFactory.getExecutorDAO().getActorAndNotTemporaryGroupsIds(user.getActor());
        if (!ApplicationContextFactory.getPermissionDAO().hasPrivilegedExecutor(executorIds)) {
            this.executorIdsToCheckPermission = executorIds;
        } else {
            this.executorIdsToCheckPermission = null;
        }
    }

    public List<Long> getExecutorIdsToCheckPermission() {
        return executorIdsToCheckPermission;
    }

    public Permission getPermission() {
        return permission;
    }

    public SecuredObjectType[] getSecuredObjectTypes() {
        return securedObjectTypes;
    }
}
