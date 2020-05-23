package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class ExecutionServiceDelegateTimerTest extends ServletTestCase {
    private static final String STATE_KOCHAB = "Kochab";
    private static final String STATE_ALIFA = "Alifa";

    private WfServiceTestHelper h;
    private ExecutionService executionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = h.getExecutionService();

        h.deployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME + ".par");
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.TIMER_PROCESS_NAME);
        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void test() {
        Long pid = prolog();
        h.getTaskService().completeTask(h.getAuthorizedUser(),
                h.getTaskService().getMyTasks(h.getAuthorizedUser(), BatchPresentationFactory.TASKS.createDefault()).get(0).getId(),
                null);
        epilog(pid, STATE_KOCHAB, 1, 0);
    }

    public void testTimeout() {
        Long pid = prolog();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }

        // TODO fix me!!! epilog(pid, STATE_ANWAR, 0, 1);
    }

    private Long prolog() {
        Long pid = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.TIMER_PROCESS_NAME, null);
        assertEquals(STATE_ALIFA,
                h.getTaskService().getMyTasks(h.getAuthorizedUser(), BatchPresentationFactory.TASKS.createDefault()).get(0).getName());
        checkTasksCount(h.getAuthorizedUser(), 1);
        checkTasksCount(h.getErpOperatorUser(), 0);
        return pid;
    }

    private void epilog(Long pid, String stateName, int reqTasksCount, int erpTasksCount) {
        List<WfTask> list = h.getTaskService().getMyTasks(h.getAuthorizedUser(), BatchPresentationFactory.TASKS.createDefault());
        for (WfTask inst : list) {
            assertEquals(stateName, inst.getName());
        }
        // assertEquals (stateName, executionService.getProcessInstanceTokens(
        // helper.getAuthorizedUser(), pid).get(1).getName());
        checkTasksCount(h.getAuthorizedUser(), reqTasksCount);
        checkTasksCount(h.getErpOperatorUser(), erpTasksCount);
    }

    private void checkTasksCount(User user, int expected) {
        assertEquals(expected, h.getTaskService().getMyTasks(user, h.getTaskBatchPresentation()).size());
    }
}
