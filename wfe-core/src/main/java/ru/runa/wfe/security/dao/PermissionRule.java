package ru.runa.wfe.security.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

@Getter
@Setter
@NoArgsConstructor
public class PermissionRule {

    private Permission permission;
    private SecuredObjectType objectType;
    private Long objectId;
    private Boolean isAdministrator;
    private Executor executor;
    
    public PermissionRule(SecuredObjectType type, Permission perm, Boolean isAdmin) {
        this.objectType = type;
        this.permission = perm;
        this.isAdministrator = isAdmin;
    }
    
    public boolean isAllowed(SecuredObjectType type, Long id, Boolean isAdmin, Permission perm) {
        if (permission != null) {
            if (! permission.equals(perm) ) {
                return false;
            }
        }
        
        if (objectType != null) {
            if ( !objectType.equals(type)) {
                return false;
            }
        }
        
        if (objectId != null) {
            if (!objectId.equals(id)) {
                return false;
            }
        }
        
        if (isAdministrator != null) {
            if (!isAdministrator.equals(isAdmin)) {
                return false;
            }
        }
        
        return true; 
    }
}
