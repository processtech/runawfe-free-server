package ru.runa.wfe.script.permission;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.google.common.base.Strings;
import ru.runa.wfe.script.common.NamedIdentitySet;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.Permission;

public abstract class ChangePermissionsOperationById extends ChangePermissionsOperation {

    protected Set<Long> ids = null;

    @Override
    public void validate(ScriptExecutionContext context) {
        if (xmlNamedIdentitySets.isEmpty() == (Strings.isNullOrEmpty(xmlId) && Strings.isNullOrEmpty(xmlType))) {
            throw new ScriptValidationException("Either attributes pair of 'type' and 'id' or child element 'namedIdentitySet' must be present, but not both");
        }
        
        if (xmlNamedIdentitySets.isEmpty()) {
            if (Strings.isNullOrEmpty(xmlId)) {
                throw new ScriptValidationException("Attribute 'id' in the " + getClass().getSimpleName() + " is missing");
            } else {
                try {
                    ids = Collections.singleton(Long.valueOf(xmlId));
                } catch (NumberFormatException e){
                    throw new ScriptValidationException("Attribute 'id' value in the " + getClass().getSimpleName() + " is not numeric");
                }
            }
            if (Strings.isNullOrEmpty(xmlType)) {
                throw new ScriptValidationException("Attribute 'xmlType' in the " + getClass().getSimpleName() + " is missing");
            } else {
                if (SecuredObjectType.valueOf(xmlType).isSingleton() ) {
                    throw new ScriptValidationException("SecuredObjectType " + xmlType + " is a Singleton thus cannot be found by Id");
                }
            }
        } else {
            ids = new HashSet<>();
            for (NamedIdentitySet namedIdentitySet : xmlNamedIdentitySets) {
                xmlType = namedIdentitySet.type.getSecuredObjectType().getName();
                Set<String> idsFromNamedIdentitySet = namedIdentitySet.get(context);
                for (String id : idsFromNamedIdentitySet) {
                    try {
                        ids.add(Long.valueOf(id));
                    } catch (NumberFormatException e) {
                        throw new ScriptValidationException("Attribute 'id' value in the 'Identity' is not numeric or empty");
                    }
                }
            }
        }

        if (xmlExecutor == null || xmlExecutor.isEmpty()) {
            throw new ScriptValidationException("Attribute 'executor' in the " + getClass().getSimpleName() + " is missing");
        }
        
        if (this instanceof RemoveAllPermissionsOperationById) {
        	permissions = Collections.emptySet();
        } else if (xmlPermissions.isEmpty()) {
            throw new ScriptValidationException("There are no one 'permission' child element in the " + getClass().getSimpleName());
        } else {
            permissions = new HashSet<>(xmlPermissions.size());
                for (ru.runa.wfe.script.permission.Permission xp : xmlPermissions) {
                Permission p = Permission.valueOf(xp.name);
                ApplicablePermissions.check(SecuredObjectType.valueOf(xmlType), p);
                permissions.add(p);
            }
        }
    }
}
