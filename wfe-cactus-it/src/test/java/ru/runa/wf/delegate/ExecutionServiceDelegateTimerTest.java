package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

import com.google.common.collect.Lists;

public class ExecutionServiceDelegateTimerTest extends ServletTestCase {

    private static final String STATE_KOCHAB = "Kochab";

    private static final String STATE_ALIFA = "Alifa";

    private ExecutionService executionService = null;

    private WfServiceTestHelper th = null;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        th.deployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME + ".par");
        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.TIMER_PROCESS_NAME);

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());

        executionService = th.getExecutionService();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void test() throws InternalApplicationException {
        Long pid = prolog();
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(),
                th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), BatchPresentationFactory.TASKS.createDefault()).get(0).getId(),
                new HashMap<String, Object>());
        epilog(pid, STATE_KOCHAB, 1, 0);
    }

    public void testTimeout() throws InternalApplicationException {
        Long pid = prolog();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }

        // TODO fix me!!! epilog(pid, STATE_ANWAR, 0, 1);
    }

    private Long prolog() throws AuthorizationException, AuthenticationException, DefinitionDoesNotExistException, ValidationException {
        Long pid = executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.TIMER_PROCESS_NAME, null);
        assertEquals(STATE_ALIFA,
                th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), BatchPresentationFactory.TASKS.createDefault()).get(0).getName());
        checkTasksCount(th.getAuthorizedPerformerUser(), 1);
        checkTasksCount(th.getErpOperatorUser(), 0);
        return pid;
    }

    private void epilog(Long pid, String stateName, int reqTasksCount, int erpTasksCount) throws AuthenticationException, AuthorizationException {
        List<WfTask> list = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), BatchPresentationFactory.TASKS.createDefault());
        for (WfTask inst : list) {
            assertEquals(stateName, inst.getName());
        }
        // assertEquals (stateName, executionService.getProcessInstanceTokens(
        // helper.getAuthorizedPerformerUser(), pid).get(1).getName());
        checkTasksCount(th.getAuthorizedPerformerUser(), reqTasksCount);
        checkTasksCount(th.getErpOperatorUser(), erpTasksCount);
    }

    private void checkTasksCount(User user, int expected) throws InternalApplicationException {
        assertEquals(expected, th.getTaskService().getMyTasks(user, th.getTaskBatchPresentation()).size());
    }
}
