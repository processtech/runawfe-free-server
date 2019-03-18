package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetVariableTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private final String variableName = "var1";

    private final String variableValue = "var1Value";

    private Long processId;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        HashMap<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, variablesMap);
        super.setUp();
    }

    private void initTaskId() throws AuthorizationException, AuthenticationException {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAdminUser(), th.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        processId = tasks.get(0).getProcessId();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition();
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetVariableByAuthorizedSubject() throws Exception {
        initTaskId();
        WfVariable variable = executionService.getVariable(th.getAuthorizedPerformerUser(), processId, variableName);
        assertEquals("variable has incorrect value", variableValue, variable.getValue());
    }
}
