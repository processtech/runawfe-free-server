package ru.runa.wfe.script.permission;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

/**
 * @author zuvladimir
 * @date 14-07-2017 15:49:06
 */
@XmlTransient()
public abstract class ChangePermissionsOnAllOperations extends ScriptOperation {

    private final SecuredObjectType securedObjectType;

    private final ChangePermissionType changeType;

    @XmlElement(name = AdminScriptConstants.PERMISSION_ELEMENT_NAME, required = true, namespace = AdminScriptConstants.NAMESPACE)
    public List<Permission> permissions = Lists.newArrayList();

    ChangePermissionsOnAllOperations() {
        this.securedObjectType = null;
        this.changeType = null;
    }

    public ChangePermissionsOnAllOperations(SecuredObjectType securedObjectType, NamedIdentityType identitiesType, ChangePermissionType changeType) {
        this.securedObjectType = securedObjectType;
        this.changeType = changeType;
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
        for (Permission permission : permissions) {
            securedObjectType.getNoPermission().getPermission(permission.name);
        }
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        Set<Identifiable> identifiables = getIdentifiables(context);
        Set<ru.runa.wfe.security.Permission> changePermissions = Sets.newHashSet();
        for (Permission permissionElement : permissions) {
            ru.runa.wfe.security.Permission permission = securedObjectType.getNoPermission().getPermission(permissionElement.name);
            changePermissions.add(permission);
        }
        for (Identifiable identifiable : identifiables) {
            Set<ru.runa.wfe.security.Permission> newPermissions = changeType.updatePermission(context, (Executor) identifiable, identifiable,
                    changePermissions);
            context.getAuthorizationLogic().setPermissions(context.getUser(), (Executor) identifiable, newPermissions, identifiable);
        }
    }

    protected abstract Set<Identifiable> getIdentifiables(ScriptExecutionContext context);

}
