package ru.runa.wfe.script.permission;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

@XmlTransient()
public abstract class ChangePermissionsOnSecuredObjectsOperation extends IdentitiesSetContainerOperation {

    private final SecuredObjectType securedObjectType;

    private final ChangePermissionType changeType;

    /**
     * Optional name for secured object, added to standard secured object set.
     */
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = false)
    public String name;

    /**
     * Executor, which grant/remove permission on secured object.
     */
    @XmlAttribute(name = AdminScriptConstants.EXECUTOR_ATTRIBUTE_NAME, required = true)
    public String executor;

    @XmlElement(name = AdminScriptConstants.PERMISSION_ELEMENT_NAME, required = true, namespace = AdminScriptConstants.NAMESPACE)
    public List<ru.runa.wfe.script.permission.Permission> permissions = Lists.newArrayList();

    ChangePermissionsOnSecuredObjectsOperation() {
        super();
        this.securedObjectType = null;
        this.changeType = null;
    }

    public ChangePermissionsOnSecuredObjectsOperation(SecuredObjectType securedObjectType, NamedIdentityType identitiesType,
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
        for (ru.runa.wfe.script.permission.Permission p : permissions) {
            ApplicablePermissions.check(securedObjectType, Permission.valueOf(p.name));
        }
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        Set<String> identityNames = getIdentityNames(context);
        if (!Strings.isNullOrEmpty(name)) {
            identityNames.add(name);
        }
        Executor grantedExecutor = context.getExecutorLogic().getExecutor(context.getUser(), executor);
        Set<SecuredObject> securedObjects = getSecuredObjects(context, identityNames);
        Set<ru.runa.wfe.security.Permission> changePermissions = Sets.newHashSet();
        for (ru.runa.wfe.script.permission.Permission permissionElement : permissions) {
            ru.runa.wfe.security.Permission p = Permission.valueOf(permissionElement.name);
            ApplicablePermissions.check(securedObjectType, p);
            changePermissions.add(p);
        }
        for (SecuredObject securedObject : securedObjects) {
            Set<Permission> newPermissions = changeType.updatePermission(context, grantedExecutor, securedObject, changePermissions);
            context.getAuthorizationLogic().setPermissions(context.getUser(), grantedExecutor, newPermissions, securedObject);
        }
    }

    protected abstract Set<SecuredObject> getSecuredObjects(ScriptExecutionContext context, Set<String> identityNames);
}
