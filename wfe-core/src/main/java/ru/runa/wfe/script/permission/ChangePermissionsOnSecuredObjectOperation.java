package ru.runa.wfe.script.permission;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.user.Executor;

@XmlTransient()
public abstract class ChangePermissionsOnSecuredObjectOperation extends ScriptOperation {

    private final SecuredObject securedObject;

    private final ChangePermissionType changeType;

    /**
     * Executor, which grant/remove permission on securedObject.
     */
    @XmlAttribute(name = AdminScriptConstants.EXECUTOR_ATTRIBUTE_NAME, required = true)
    public String executor;

    @XmlElement(name = AdminScriptConstants.PERMISSION_ELEMENT_NAME, required = true, namespace = AdminScriptConstants.NAMESPACE)
    public List<ru.runa.wfe.script.permission.Permission> permissions = Lists.newArrayList();

    ChangePermissionsOnSecuredObjectOperation() {
        this.securedObject = null;
        this.changeType = null;
    }

    public ChangePermissionsOnSecuredObjectOperation(SecuredObject securedObject, ChangePermissionType changeType) {
        this.securedObject = securedObject;
        this.changeType = changeType;
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.EXECUTOR_ATTRIBUTE_NAME, executor);
        for (ru.runa.wfe.script.permission.Permission p : permissions) {
            ru.runa.wfe.security.Permission.valueOf(p.name).checkApplicable(securedObject.getSecuredObjectType());
        }
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        Executor grantedExecutor = context.getExecutorLogic().getExecutor(context.getUser(), executor);
        Set<Permission> changePermissions = Sets.newHashSet();
        for (ru.runa.wfe.script.permission.Permission permissionElement : permissions) {
            ru.runa.wfe.security.Permission p = Permission.valueOf(permissionElement.name);
            p.checkApplicable(securedObject.getSecuredObjectType());
            changePermissions.add(p);
        }
        Set<ru.runa.wfe.security.Permission> newPermissions = changeType.updatePermission(context, grantedExecutor, securedObject, changePermissions);
        context.getAuthorizationLogic().setPermissions(context.getUser(), grantedExecutor, newPermissions, securedObject);
    }
}
