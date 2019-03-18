package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateCompleteTaskTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private WfTask task;

    private Map<String, Object> legalVariables;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());
        // task =
        // executionDelegate.getTasks(helper.getAuthorizedPerformerUser(),
        // helper.getTaskBatchPresentation())[0];

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount.asked", (double) 200);
        legalVariables.put("amount.granted", (double) 150);
        legalVariables.put("approved", "true");

        super.setUp();
    }

    private void initTask() throws AuthorizationException, AuthenticationException {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        task = tasks.get(0);
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testCompleteTaskByAuthorizedSubject() throws Exception {
        initTask();

        assertEquals("state name differs from expected", "evaluating", task.getName());
        assertEquals("task <evaluating> is assigned before completeTask()", th.getBossGroup(), task.getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), task.getId(), legalVariables, null);
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());
        assertEquals("task <treating collegues on cake and pie> is not assigned after starting [requester]", th.getBossGroup(), task.getOwner());
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), legalVariables, null);

        tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), th.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "updating erp asynchronously", tasks.get(0).getName());
        assertEquals("task <updating erp asynchronously> is not assigned before competeTask()", th.getBossGroup(), task.getOwner());
    }

    public void testCompleteTaskBySubjectWhichIsNotInSwimlane() throws Exception {
        initTask();
        try {
            th.removeExecutorFromGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());
            th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), task.getId(), legalVariables, null);
            fail("testCompleteTaskByNullSubject(), no Exception");
        } catch (AuthorizationException e) {
        }
    }

    public void testCompleteTaskByUnauthorizedSubject() throws Exception {
        initTask();
        try {
            th.getTaskService().completeTask(th.getUnauthorizedPerformerUser(), task.getId(), legalVariables, null);
            fail("testCompleteTaskByNullSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testCompleteTaskByFakeSubject() throws Exception {
        initTask();
        try {
            th.getTaskService().completeTask(th.getFakeUser(), task.getId(), legalVariables, null);
            fail("testCompleteTaskByFakeSubject(), no AuthenticationException");
        } catch (AuthorizationException e) {
            fail("testCompleteTaskByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithInvalidTaskId() throws Exception {
        initTask();
        try {
            th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), -1l, legalVariables, null);
            fail("testCompleteTaskByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException");
        } catch (TaskDoesNotExistException e) {
        }
    }
}
