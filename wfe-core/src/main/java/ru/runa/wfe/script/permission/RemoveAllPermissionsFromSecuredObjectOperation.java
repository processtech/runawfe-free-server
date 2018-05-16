package ru.runa.wfe.script.permission;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.user.Executor;

@XmlTransient()
public abstract class RemoveAllPermissionsFromSecuredObjectOperation extends ScriptOperation {

    private final String elementName;

    private final SecuredObject securedObject;

    RemoveAllPermissionsFromSecuredObjectOperation() {
        this.elementName = null;
        this.securedObject = null;
    }

    public RemoveAllPermissionsFromSecuredObjectOperation(String elementName, SecuredObject securedObject) {
        this.elementName = elementName;
        this.securedObject = securedObject;
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<? extends Executor> executors = context.getAuthorizationLogic().getExecutorsWithPermission(context.getUser(), securedObject,
            batchPresentation, true);
        for (Executor executor : executors) {
            context.getAuthorizationLogic().setPermissions(context.getUser(), executor, new ArrayList<Permission>(), securedObject);
        }
    }
}
