package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
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

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ,
                DefinitionPermission.READ_STARTED_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        // instanceId =
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());
        instanceId = executionService.getProcesses(th.getAdminUser(), th.getProcessInstanceBatchPresentation()).get(0).getId();

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount.asked", new Double(200));
        legalVariables.put("amount.granted", new Double(150));
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

    public void testGetSwimlaneExecutorMapByNullSubject() throws Exception {
        try {
            th.getTaskService().getProcessTasks(null, instanceId, true);
            fail("testGetSwimlaneExecutorMapByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            th.getTaskService().getProcessTasks(th.getAuthorizedPerformerUser(), -1l, true);
            fail("testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId(), no ProcessInstanceDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
        }
    }

    //
    // public void testGetSwimlaneExecutorMapByAuthorizedSubject() throws
    // Exception {
    // Collection<Permission> readPermissions =
    // Lists.newArrayList(Permission.READ);
    // helper.setPermissionsToAuthorizedPerformer(readPermissions,
    // helper.getErpOperator());
    //
    // List<Swimlane> swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    //
    // swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // for (Swimlane swimlane : swimlanes) {
    // Map<String, Executor> executorsInSwimlane =
    // executionService.getActiveTasks(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // for (String name : executorsInSwimlane.keySet()) {
    // Assert.assertEquals("Executor in the swimlane differs from expected",
    // getExpectedExecutor(swimlane), executorsInSwimlane.get(name));
    // }
    // }
    //
    // WfTask task =
    // th.getTaskService().getMyTasks(helper.getAuthorizedPerformerUser(),
    // helper.getTaskBatchPresentation()).get(0);
    // th.getTaskService().completeTask(helper.getAuthorizedPerformerUser(),
    // task.getId(), legalVariables);
    //
    // swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // for (Swimlane swimlane : swimlanes) {
    // Map<String, Executor> executorsInSwimlane =
    // executionService.getActiveTasks(helper.getAuthorizedPerformerUser(),
    // instanceId,
    // swimlane.getDefinition().getName());
    // for (String name : executorsInSwimlane.keySet()) {
    // Assert.assertEquals("Executor in the swimlane differs from expected",
    // getExpectedExecutor(swimlane), executorsInSwimlane.get(name));
    // }
    // }
    // }
    //
    // public void testGetSwimlaneExecutorMapDeletedExecutor() throws Exception
    // {
    // WfTask task =
    // th.getTaskService().getMyTasks(helper.getAuthorizedPerformerUser(),
    // helper.getTaskBatchPresentation()).get(0);
    // th.getTaskService().completeTask(helper.getAuthorizedPerformerUser(),
    // task.getId(), legalVariables);
    // List<Swimlane> swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // Swimlane swimlane = null;
    // for (Swimlane existing : swimlanes) {
    // if ("erp operator".equals(existing.getDefinition().getName())) {
    // swimlane = existing;
    // break;
    // }
    // }
    // assert (swimlane != null);
    // helper.removeCreatedExecutor(helper.getErpOperator());
    // helper.removeExecutorIfExists(helper.getErpOperator());
    // try {
    // executionService.getActiveTasks(helper.getAuthorizedPerformerUser(),
    // instanceId, swimlane.getDefinition().getName());
    // fail("executionDelegate.getSwimlaneExecutorMap() does not throw exception for getting swimlane for nonexisting executor");
    // } catch (ExecutorDoesNotExistException e) {
    // }
    // }

    private Executor getExpectedExecutor(WfSwimlane WfSwimlane) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
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
