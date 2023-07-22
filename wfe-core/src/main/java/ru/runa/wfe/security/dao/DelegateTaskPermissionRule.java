package ru.runa.wfe.security.dao;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;

public class DelegateTaskPermissionRule extends PermissionRule {

    public DelegateTaskPermissionRule() {
        super();
    }

    public DelegateTaskPermissionRule(SecuredObjectType type, Permission perm, Boolean isAdmin) {
        super(type, perm, isAdmin);
    }

    @Override
    public boolean isAllowed(SecuredObjectType type, Long id, Boolean isAdmin, Permission perm) {
        if (SystemProperties.isTaskDelegationEnabled()) {
            return super.isAllowed(type, id, isAdmin, perm);    
        } else {
            return false;
        }
    }

}
