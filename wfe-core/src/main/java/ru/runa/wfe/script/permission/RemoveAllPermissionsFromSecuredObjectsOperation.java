package ru.runa.wfe.script.permission;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentitiesSetContainerOperation;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.user.Executor;

@XmlTransient()
public abstract class RemoveAllPermissionsFromSecuredObjectsOperation extends IdentitiesSetContainerOperation {

    /**
     * Optional name for secured object, added to standard secured object set.
     */
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = false)
    public String name;

    public RemoveAllPermissionsFromSecuredObjectsOperation() {
        super();
    }

    public RemoveAllPermissionsFromSecuredObjectsOperation(NamedIdentityType identitiesType) {
        super(identitiesType);
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
        super.validate(false);
        if (Strings.isNullOrEmpty(name) && !super.isStandartIdentitiesSetDefined()) {
            throw new ScriptValidationException(this, "Required " + AdminScriptConstants.NAME_ATTRIBUTE_NAME + " or "
                    + AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME + " elements.");
        }
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        Set<String> identityNames = getIdentityNames(context);
        if (!Strings.isNullOrEmpty(name)) {
            identityNames.add(name);
        }
        for (SecuredObject securedObject : getSecuredObjects(context, identityNames)) {

            BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            List<? extends Executor> executors = context.getAuthorizationLogic().getExecutorsWithPermission(context.getUser(), securedObject,
                batchPresentation, true);
            for (Executor executor : executors) {
                context.getAuthorizationLogic().setPermissions(context.getUser(), executor, new ArrayList<Permission>(), securedObject);
            }
        }
    }

    protected abstract Set<SecuredObject> getSecuredObjects(ScriptExecutionContext context, Set<String> identityNames);
}
