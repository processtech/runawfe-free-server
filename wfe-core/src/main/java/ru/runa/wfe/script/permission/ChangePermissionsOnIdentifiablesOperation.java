package ru.runa.wfe.script.permission;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentitiesSetContainerOperation;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlTransient()
public abstract class ChangePermissionsOnIdentifiablesOperation extends IdentitiesSetContainerOperation {

    private final SecuredObjectType securedObjectType;

    private final ChangePermissionType changeType;

    /**
     * Optional name for identifiable, added to standard identifiable set.
     */
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = false)
    public String name;

    /**
     * Executor, which grant/remove permission on identifiable.
     */
    @XmlAttribute(name = AdminScriptConstants.EXECUTOR_ATTRIBUTE_NAME, required = true)
    public String executor;

    @XmlElement(name = AdminScriptConstants.PERMISSION_ELEMENT_NAME, required = true, namespace = AdminScriptConstants.NAMESPACE)
    public List<Permission> permissions = Lists.newArrayList();

    ChangePermissionsOnIdentifiablesOperation() {
        super();
        this.securedObjectType = null;
        this.changeType = null;
    }

    public ChangePermissionsOnIdentifiablesOperation(SecuredObjectType securedObjectType, NamedIdentityType identitiesType,
            ChangePermissionType changeType) {
        super(identitiesType);
        this.securedObjectType = securedObjectType;
        this.changeType = changeType;
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
        super.validate(false);
        if (Strings.isNullOrEmpty(name) && !super.isStandartIdentitiesSetDefined()) {
            throw new ScriptValidationException(this, "Required " + AdminScriptConstants.NAME_ATTRIBUTE_NAME + " or "
                    + AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME + " elements.");
        }
        for (Permission permission : permissions) {
            securedObjectType.getNoPermission().getPermission(permission.name);
        }
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        Set<String> identityNames = getIdentityNames(context);
        if (!Strings.isNullOrEmpty(name)) {
            identityNames.add(name);
        }
        Executor grantedExecutor = context.getExecutorLogic().getExecutor(context.getUser(), executor);
        Set<Identifiable> identifiables = getIdentifiables(context, identityNames);
        Set<ru.runa.wfe.security.Permission> changePermissions = Sets.newHashSet();
        for (Permission permissionElement : permissions) {
            ru.runa.wfe.security.Permission permission = securedObjectType.getNoPermission().getPermission(permissionElement.name);
            changePermissions.add(permission);
        }
        for (Identifiable identifiable : identifiables) {
            Set<ru.runa.wfe.security.Permission> newPermissions = changeType.updatePermission(context, grantedExecutor, identifiable,
                changePermissions);
            context.getAuthorizationLogic().setPermissions(context.getUser(), grantedExecutor, newPermissions, identifiable);
        }
    }

    protected abstract Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames);
}
