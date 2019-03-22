package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.collect.Lists;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetSwimlaneExecutorMapTest extends ServletTestCase {
    private ExecutionService executionService;
    private WfServiceTestHelper th = null;
    private Long instanceId;
    private HashMap<String, Object> legalVariables;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());
        instanceId = executionService.getProcesses(th.getAdminUser(), th.getProcessInstanceBatchPresentation()).get(0).getId();

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount_asked", new Double(200));
        legalVariables.put("amount_granted", new Double(150));
        legalVariables.put("approved", "true");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetSwimlaneExecutorMapByUnauthorizedSubject() throws Exception {
        try {
            th.getTaskService().getProcessTasks(th.getUnauthorizedPerformerUser(), instanceId, true);
            fail("testGetSwimlaneExecutorMapByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByFakeSubject() throws Exception {
        try {
            th.getTaskService().getProcessTasks(th.getFakeUser(), instanceId, true);
            fail("testGetSwimlaneExecutorMapByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            th.getTaskService().getProcessTasks(th.getAuthorizedPerformerUser(), -1l, true);
            fail("testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId(), no ProcessInstanceDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
        }
    }

    private Executor getExpectedExecutor(WfSwimlane WfSwimlane)
            throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        String name = WfSwimlane.getDefinition().getName();
        if (name.equals("requester")) {
            return th.getAuthorizedPerformerActor();
        } else if (name.equals("boss")) {
            return th.getBossGroup();
        } else if (name.equals("erp operator")) {
            return th.getErpOperator();
        } else {
            throw new RuntimeException("Executor for swimlane " + WfSwimlane.getDefinition().getName() + " is unknown");
        }
    }
}
