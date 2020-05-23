package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.val;
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
 * It does not take substitution logic into account.
 * 
 * @see ExecutionServiceDelegateSubstitutionAssignTaskTest
 */
public class ExecutionServiceDelegateAssignTaskTest extends ServletTestCase {
    private static final String PROCESS_NAME = WfServiceTestHelper.SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME;

    private final static String nameActor1 = "actor1";
    private final static String nameActor2 = "actor2";
    private final static String nameGroup = "testGroup";

    private final static String pwdActor1 = "123";
    private final static String pwdActor2 = "123";

    private User actor1User = null;
    private User actor2User = null;

    private WfServiceTestHelper h;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        val prefix = getClass().getName();
        h = new WfServiceTestHelper(prefix);
        batchPresentation = h.getTaskBatchPresentation();

        Actor actor1 = h.createActorIfNotExist(nameActor1, prefix);
        h.getExecutorService().setPassword(h.getAdminUser(), actor1, pwdActor1);
        Actor actor2 = h.createActorIfNotExist(nameActor2, prefix);
        h.getExecutorService().setPassword(h.getAdminUser(), actor2, pwdActor2);
        Group group = h.createGroupIfNotExist(nameGroup, "description");
        h.addExecutorToGroup(actor1, group);
        h.addExecutorToGroup(actor2, group);

        {
            val pp = Lists.newArrayList(Permission.LOGIN);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), group.getId(), pp, SecuredSingleton.SYSTEM);  // TODO What for?
            h.getAuthorizationService().setPermissions(h.getAdminUser(), actor1.getId(), pp, SecuredSingleton.SYSTEM);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), actor2.getId(), pp, SecuredSingleton.SYSTEM);
        }

        actor1User = h.getAuthenticationService().authenticateByLoginPassword(nameActor1, pwdActor1);
        actor2User = h.getAuthenticationService().authenticateByLoginPassword(nameActor2, pwdActor2);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_NAME + ".par");
        h.getDefinitionService().deployProcessDefinition(h.getAdminUser(), parBytes, Lists.newArrayList("testProcess"), null);
        WfDefinition definition = h.getDefinitionService().getLatestProcessDefinition(h.getAdminUser(), PROCESS_NAME);
        h.getAuthorizationService().setPermissions(h.getAdminUser(), actor1.getId(), Lists.newArrayList(Permission.START_PROCESS), definition);
    }

    @Override
    protected void tearDown() {
        h.getDefinitionService().undeployProcessDefinition(h.getAdminUser(), PROCESS_NAME, null);
        h.releaseResources();
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 assigns a task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     */
    // 1
    public void testAssignAssigned() {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
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
     */
    public void testAssignMoved() {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
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
     */
    public void testMoveAssigned() {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
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
     */
    public void testMoveMoved() {
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2User, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        moveExecuteExecuted();
    }

    private void moveAssignAssigned() {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        Actor actor = actor1User.getActor();
        h.getTaskService().assignTask(actor1User, tasks1[0].getId(), tasks1[0].getOwner(), actor);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        assertExceptionThrownOnAssign(actor2User, tasks2[0]);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        h.getTaskService().completeTask(actor1User, tasks1[0].getId(), null);
    }

    private void moveAssignMoved() {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        h.getTaskService().completeTask(actor1User, tasks1[0].getId(), null);
        assertExceptionThrownOnAssign(actor2User, tasks2[0]);
    }

    // ------------------------------------------------------------------------------------------------------------------------
    private void moveMoveAssigned() {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        Actor actor = actor1User.getActor();
        h.getTaskService().assignTask(actor1User, tasks1[0].getId(), tasks1[0].getOwner(), actor);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        assertExceptionThrownOnExecute(actor2User, tasks2[0]);
        {
            tasks1 = checkTaskList(actor1User, 1);
            checkTaskList(actor2User, 0);
        }
        h.getTaskService().completeTask(actor1User, tasks1[0].getId(), null);
    }

    private void moveExecuteExecuted() {
        WfTask[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1User, 1);
            tasks2 = checkTaskList(actor2User, 1);
        }
        h.getTaskService().completeTask(actor1User, tasks1[0].getId(), null);
        {
            checkTaskList(actor1User, tasks1[0]);
            checkTaskList(actor2User, tasks2[0]);
        }
        assertExceptionThrownOnExecute(actor2User, tasks2[0]);
    }

    private void assertExceptionThrownOnExecute(User user, WfTask task) {
        try {
            h.getTaskService().completeTask(user, task.getId(), null);
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
    private void assertExceptionThrownOnAssign(User user, WfTask task) {
        try {
            Actor actor = actor1User.getActor();
            h.getTaskService().assignTask(user, task.getId(), task.getOwner(), actor);
            throw new InternalApplicationException("Exception TaskAlreadyAcceptedException not thrown");
        } catch (TaskAlreadyAcceptedException e) {
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        }
    }

    private List<WfTask> checkTaskList(User user, WfTask task) {
        boolean result = false;
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        for (WfTask taskStub : tasks) {
            if (taskStub.equals(task) && taskStub.getName().equals(task.getName())) {
                result = true;
                break;
            }
        }
        assertFalse("Executed task is still in the user's tasks list.", result);
        return tasks;
    }

    private WfTask[] checkTaskList(User user, int expectedLength) {
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        return tasks.toArray(new WfTask[tasks.size()]);
    }
}
