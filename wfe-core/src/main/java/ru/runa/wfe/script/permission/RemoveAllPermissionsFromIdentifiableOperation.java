package ru.runa.wfe.script.permission;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;

@XmlTransient()
public abstract class RemoveAllPermissionsFromIdentifiableOperation extends ScriptOperation {

    private final String elementName;

    private final Identifiable identifiable;

    RemoveAllPermissionsFromIdentifiableOperation() {
        this.elementName = null;
        this.identifiable = null;
    }

    public RemoveAllPermissionsFromIdentifiableOperation(String elementName, Identifiable identifiable) {
        this.elementName = elementName;
        this.identifiable = identifiable;
    }

    @Override
    public final void validate(ScriptExecutionContext context) {
    }

    @Override
    public final void execute(ScriptExecutionContext context) {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<? extends Executor> executors = context.getAuthorizationLogic().getExecutorsWithPermission(context.getUser(), identifiable,
            batchPresentation, true);
        for (Executor executor : executors) {
            context.getAuthorizationLogic().setPermissions(context.getUser(), executor, new ArrayList<Permission>(), identifiable);
        }
    }
}
