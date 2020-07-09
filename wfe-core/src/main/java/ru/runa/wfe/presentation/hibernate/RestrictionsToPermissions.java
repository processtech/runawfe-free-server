package ru.runa.wfe.presentation.hibernate;

import java.util.Arrays;

import org.springframework.util.Assert;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecurityCheckProperties;
import ru.runa.wfe.user.User;

/**
 * Restrictions to load only objects with specified permission granted for user.
 */
public class RestrictionsToPermissions {

    protected RestrictionsToPermissions cloneCheckRequired() {
        SecuredObjectType[] resTypes = null;
        if (this.types != null) {
            resTypes = new SecuredObjectType[types.length];
            int n = 0;
            for (int i = 0; i < types.length; i++) {
                if (types[i] != null && SecurityCheckProperties.isPermissionCheckRequired(types[i])) {
                    resTypes[n] = types[i];
                    n++;
                }                
            }
            if (n == 0) {
                return null;
            } else {
                resTypes = Arrays.copyOf(resTypes, n);
            }
        }
        RestrictionsToPermissions res = new RestrictionsToPermissions(this.user, this.permission, resTypes);        
        return res;
    }

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
