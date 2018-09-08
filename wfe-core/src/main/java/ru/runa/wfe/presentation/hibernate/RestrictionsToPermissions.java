package ru.runa.wfe.presentation.hibernate;

import org.springframework.util.Assert;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.User;

/**
 * Restrictions to load only objects with specified permission granted for user.
 */
public class RestrictionsToPermissions {
    /**
     * User which must has permission on queried objects. Queries only objects with condition: at least one executor from (user + its groups)
     * must have 'permission' with 'securedObjectTypes'. Can be null.
     */
    public final User user;

    /**
     * Permission, which at least one executors must has on queried objects. Queries only objects with condition: at least one executor from
     * executorIds must have 'permission' with 'securedObjectTypes'
     */
    public final Permission permission;

    /**
     * Type of secured object for queried objects. Queries only objects with condition: at least one executor from executorIds must have 'permission'
     * with 'securedObjectTypes'
     */
    public final SecuredObjectType[] types;

    public RestrictionsToPermissions(User user, Permission permission, SecuredObjectType[] types) {
        Assert.notNull(user);
        Assert.notNull(permission);
        Assert.notEmpty(types);
        this.user = user;
        this.permission = permission;
        this.types = types;
    }
}
