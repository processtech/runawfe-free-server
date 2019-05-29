package ru.runa.wfe.security;

import com.google.common.collect.Lists;
import java.util.Collection;
import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that {@link java.security.Permission}s are not applicable on selected SecuredObjectType.
 * 
 */
public class UnapplicablePermissionException extends InternalApplicationException {
    private static final long serialVersionUID = 8758756795316935351L;
    private final Collection<Permission> permissions;

    public UnapplicablePermissionException(SecuredObjectType type, Collection<Permission> permissions) {
        super(permissions + " are not applicable for " + type.getName());
        this.permissions = Lists.newArrayList(permissions);
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }
}
