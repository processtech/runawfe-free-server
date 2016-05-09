package ru.runa.wfe.script.permission;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlTransient()
public abstract class ChangePermissionsOnIdentifiableOperation extends ScriptOperation {

    private final Identifiable identifiable;

    private final ChangePermissionType changeType;

    /**
     * Executor, which grant/remove permission on identifiable.
     */
    @XmlAttribute(name = AdminScriptConstants.EXECUTOR_ATTRIBUTE_NAME, required = true)
    public String executor;

    @XmlElement(name = AdminScriptConstants.PERMISSION_ELEMENT_NAME, required = true, namespace = AdminScriptConstants.NAMESPACE)
    public List<Permission> permissions = Lists.newArrayList();

    ChangePermissionsOnIdentifiableOperation() {
        this.identifiable = null;
        this.changeType = null;
    }

    public ChangePermissionsOnIdentifiableOperation(Identifiable identifiable, ChangePermissionType changeType) {
        this.identifiable = identifiable;
        this.changeType = changeType;
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.EXECUTOR_ATTRIBUTE_NAME, executor);
        for (Permission permission : permissions) {
            identifiable.getSecuredObjectType().getNoPermission().getPermission(permission.name);
        }
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        Executor grantedExecutor = context.getExecutorLogic().getExecutor(context.getUser(), executor);
        Set<ru.runa.wfe.security.Permission> changePermissions = Sets.newHashSet();
        for (Permission permissionElement : permissions) {
            ru.runa.wfe.security.Permission permission = identifiable.getSecuredObjectType().getNoPermission().getPermission(permissionElement.name);
            changePermissions.add(permission);
        }
        Set<ru.runa.wfe.security.Permission> newPermissions = changeType.updatePermission(context, grantedExecutor, identifiable, changePermissions);
        context.getAuthorizationLogic().setPermissions(context.getUser(), grantedExecutor, newPermissions, identifiable);
    }
}
