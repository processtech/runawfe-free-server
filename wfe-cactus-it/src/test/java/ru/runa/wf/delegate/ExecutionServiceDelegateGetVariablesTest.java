package ru.runa.wf.delegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
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
public class ExecutionServiceDelegateGetVariablesTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private Long taskId;

    private Long processId;

    private final String variableName = "var1";

    private final String variableValue = "var1Value";

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        HashMap<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        processId = executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());
        WfTask taskStub = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation()).get(0);
        taskId = taskStub.getId();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetVariablesByUnauthorizedSubject() throws Exception {
        try {
            executionService.getVariables(th.getUnauthorizedPerformerUser(), processId);
            fail("testGetVariablesByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetVariablesByFakeSubject() throws Exception {
        try {
            executionService.getVariables(th.getFakeUser(), processId);
            fail("testGetVariablesByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetVariablesByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getVariables(th.getAuthorizedPerformerUser(), -1l);
            fail("testGetVariablesByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
        }
    }

    public void testGetVariablesByAuthorizedSubject() throws Exception {
        List<WfVariable> variables = executionService.getVariables(th.getAuthorizedPerformerUser(), processId);
        List<String> names = new ArrayList<String>();
        for (WfVariable v : variables) {
            names.add(v.getDefinition().getName());
        }

        List<String> expectedNames = Lists.newArrayList(variableName);
        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, names);

        HashMap<String, Object> variables2 = new HashMap<String, Object>();
        variables2.put("var2", "var2Value");
        variables2.put("var3", "var3Value");
        variables2.put("approved", "true");
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), taskId, variables2);

        taskId = th.getTaskService().getMyTasks(th.getErpOperatorUser(), th.getTaskBatchPresentation()).get(0).getId();

        variables = executionService.getVariables(th.getAdminUser(), processId);

        names = new ArrayList<String>();
        HashMap<String, Object> vars = new HashMap<String, Object>();
        for (WfVariable v : variables) {
            names.add(v.getDefinition().getName());
            vars.put(v.getDefinition().getName(), v.getValue());
        }
        expectedNames = Lists.newArrayList("var2", "var3", "approved", variableName);
        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, names);

        assertEquals(" variable value: <var1> differs from expected", "var1Value", vars.get("var1"));
        assertEquals(" variable value: <var2> differs from expected", "var2Value", vars.get("var2"));
        assertEquals(" variable value: <var3> differs from expected", "var3Value", vars.get("var3"));
        assertEquals(" variable value: <approved> differs from expected", true, vars.get("approved"));
    }
}
