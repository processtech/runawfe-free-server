package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

/**
 * This test class is to check concurrent work of 2 users concerning "Assign task" function.<br />
 * It does not take into account substitution logic.
 * 
 * @see ExecutionServiceDelegateSubstitutionAssignTaskTest
 */
public class ExecutionServiceDelegateAssignTaskTest extends ServletTestCase {

    private final static String PREFIX = ExecutionServiceDelegateAssignTaskTest.class.getName();

    private static final String PROCESS_NAME = WfServiceTestHelper.SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME;

    private final static String nameActor1 = "actor1";
    private final static String nameActor2 = "actor2";
    private final static String nameGroup = "testGroup";

    private final static String pwdActor1 = "123";
    private final static String pwdActor2 = "123";

    private Actor actor1;
    private Actor actor2;
    private Group group;

    private User actor1User = null;
    private User actor2User = null;

    private WfServiceTestHelper testHelper;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        testHelper = new WfServiceTestHelper(PREFIX);

        actor1 = testHelper.createActorIfNotExist(nameActor1, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminUser(), actor1, pwdActor1);
        actor2 = testHelper.createActorIfNotExist(nameActor2, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminUser(), actor2, pwdActor2);
        group = testHelper.createGroupIfNotExist(nameGroup, "description");
        testHelper.addExecutorToGroup(actor1, group);
        testHelper.addExecutorToGroup(actor2, group);

        {
            Collection<Permission> perm = Lists.newArrayList(Permission.LOGIN);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), group.getId(), perm, SecuredSingleton.EXECUTORS);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor1.getId(), perm, SecuredSingleton.EXECUTORS);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor2.getId(), perm, SecuredSingleton.EXECUTORS);
        }

        actor1User = testHelper.getAuthenticationService().authenticateByLoginPassword(nameActor1, pwdActor1);
        actor2User = testHelper.getAuthenticationService().authenticateByLoginPassword(nameActor2, pwdActor2);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_NAME + ".par");
        testHelper.getDefinitionService().deployProcessDefinition(testHelper.getAdminUser(), parBytes, Lists.newArrayList("testProcess"), null);
        WfDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinition(testHelper.getAdminUser(), PROCESS_NAME);
        Collection<Permission> definitionPermission = Lists.newArrayList(Permission.START);
        testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor1.getId(), definitionPermission, definition);

        batchPresentation = testHelper.getTaskBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        testHelper.getDefinitionService().undeployProcessDefinition(testHelper.getAdminUser(), PROCESS_NAME, null);
        testHelper.releaseResources();
        super.tearDown();
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 assigns a task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    // 1
    public void testAssignAssigned() throws Exception {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        for (int i = 0; i < 3; ++i) {
            moveAssignAssigned();
        }
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 executes a task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testAssignMoved() throws Exception {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        for (int i = 0; i < 3; ++i) {
            try {
                moveAssignMoved();
                fail("TODO trap");
            } catch (TaskDoesNotExistException e) {
                // TODO
                return;
            }
        }
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 assign a task</li>
     * <li>User 2 tries to move the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testMoveAssigned() throws Exception {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        for (int i = 0; i < 3; ++i) {
            moveMoveAssigned();
        }
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 executes a task</li>
     * <li>User 2 tries to execute the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testMoveMoved() throws Exception {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        moveExecuteExecuted();
    }

    private void moveAssignAssigned() throws Exception {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        Actor actor = actor1User.getActor();
        testHelper.getTaskService().assignTask(actor1User, tasks1[0].getId(), tasks1[0].getOwner(), actor);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        assertExceptionThrownOnAssign(actor2User, tasks2[0]);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        testHelper.getTaskService().completeTask(actor1User, tasks1[0].getId(), new HashMap<String, Object>());
    }

    private void moveAssignMoved() throws Exception {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        testHelper.getTaskService().completeTask(actor1User, tasks1[0].getId(), new HashMap<String, Object>());
        assertExceptionThrownOnAssign(actor2User, tasks2[0]);
    }

    // ------------------------------------------------------------------------------------------------------------------------
    private void moveMoveAssigned() throws Exception {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        Actor actor = actor1User.getActor();
        testHelper.getTaskService().assignTask(actor1User, tasks1[0].getId(), tasks1[0].getOwner(), actor);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        assertExceptionThrownOnExecute(actor2User, tasks2[0]);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        testHelper.getTaskService().completeTask(actor1User, tasks1[0].getId(), new HashMap<String, Object>());
    }

    private void moveExecuteExecuted() throws Exception {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        testHelper.getTaskService().completeTask(actor1User, tasks1[0].getId(), new HashMap<String, Object>());
        {
            checkTaskList(actor1User, tasks1[0]);
            checkTaskList(actor2User, tasks2[0]);
        }
        assertExceptionThrownOnExecute(actor2User, tasks2[0]);
    }

    private void assertExceptionThrownOnExecute(User user, WfTask task) throws InternalApplicationException {
        try {
            testHelper.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
            throw new InternalApplicationException("Exception TaskDoesNotExistException not thrown");
        } catch (AuthenticationException e) {
        } catch (AuthorizationException e) {
        } catch (TaskDoesNotExistException e) {
        } catch (ExecutorDoesNotExistException e) {
            throw new InternalApplicationException("ExecutorOutOfDateException exception thrown");
        } catch (ValidationException e) {
            throw new InternalApplicationException("ValidationException exception thrown");
        }
    }

    // /rask:
    private void assertExceptionThrownOnAssign(User user, WfTask task) throws ExecutorDoesNotExistException {
        try {
            Actor actor = actor1User.getActor();
            testHelper.getTaskService().assignTask(user, task.getId(), task.getOwner(), actor);
            throw new InternalApplicationException("Exception TaskAlreadyAcceptedException not thrown");
        } catch (TaskAlreadyAcceptedException e) {
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        }
    }

    private List<WfTask> checkTaskList(User user, WfTask task) throws Exception {
        boolean result = false;
        List<WfTask> tasks = testHelper.getTaskService().getMyTasks(user, batchPresentation);
        for (WfTask taskStub : tasks) {
            if (taskStub.equals(task) && taskStub.getName().equals(task.getName())) {
                result = true;
                break;
            }
        }
        assertFalse("Executed task is still in the user's tasks list.", result);
        return tasks;
    }

    private WfTask[] checkTaskList(User user, int expectedLength) throws Exception {
        List<WfTask> tasks = testHelper.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        return tasks.toArray(new WfTask[tasks.size()]);
    }
}
