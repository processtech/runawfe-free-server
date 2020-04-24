package ru.runa.wf.service;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

public class WfScriptServiceTestHelper extends WfServiceTestHelper {

    public WfScriptServiceTestHelper(String testClassPrefixName) {
        super(testClassPrefixName);
    }

    public boolean hasOwnPermission(Executor executor, Permission permission, SecuredObject securedObject) {
        Collection<Permission> permissions = authorizationService.getIssuedPermissions(adminUser, executor, securedObject);
        return permissions.contains(permission);
    }

    public boolean hasOwnPermissionOnDefinition(Executor executor, Permission permission, String processDefinitionName) {
        WfDefinition definition = definitionService.getLatestProcessDefinition(adminUser, processDefinitionName);
        return hasOwnPermission(executor, permission, definition);
    }

    public boolean areExecutorsWeaklyEqual(Executor e1, Executor e2) {
        if (e1 == null || e2 == null) {
            return false;
        }
        if (!Objects.equal(e1.getName(), e1.getName())) {
            return false;
        }
        if (!Objects.equal(e1.getDescription(), e1.getDescription())) {
            return false;
        }
        if ((e1 instanceof Actor) && (e2 instanceof Actor)) {
            Actor a1 = (Actor) e1;
            Actor a2 = (Actor) e2;
            if (!Objects.equal(a1.getFullName(), a2.getFullName())) {
                return false;
            }
        }
        return true;
    }

    public void executeScript(String resourceName) {
        Delegates.getScriptingService().executeAdminScript(adminUser, readBytesFromFile(resourceName), new HashMap<>());
    }

    public WfProcess startProcessInstance(String processDefinitionName, Executor performer) {
        getAuthorizationService().setPermissions(adminUser, performer.getId(),
                Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS),
                getDefinitionService().getLatestProcessDefinition(adminUser, processDefinitionName));
        getExecutionService().startProcess(adminUser, processDefinitionName, null);
        return getExecutionService().getProcesses(adminUser, getProcessInstanceBatchPresentation()).get(0);
    }
}
