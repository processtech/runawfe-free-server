package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

import com.google.common.collect.Lists;

/**
 * This test class is to check substitution logic concerning "Assign task"
 * function.<br />
 * It does not take into account concurrent work of the several members of the
 * same group.
 * 
 * @see ExecutionServiceDelegateAssignTaskTest
 */
public class ExecutionServiceDelegateSubstitutionAssignTaskTest extends ServletTestCase {

    private final static String PREFIX = ExecutionServiceDelegateSubstitutionAssignTaskTest.class.getName();

    private static final String PROCESS_NAME = WfServiceTestHelper.SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME;

    private final static String nameActor1 = "actor1";
    private final static String nameActor2 = "actor2";
    private final static String nameGroup = "testGroup";
    private final static String nameSubstitute = "substitute";

    private final static String pwdActor1 = "123";
    private final static String pwdActor2 = "123";
    private final static String pwdSubstitute = "123";

    private Actor actor1;
    private Actor actor2;
    private Group group;
    private Actor substitute;

    private User actor1User = null;
    private User actor2SUser = null;
    private User substituteUser = null;

    private SubstitutionCriteria substitutionCriteria_always;

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
        substitute = testHelper.createActorIfNotExist(nameSubstitute, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminUser(), substitute, pwdSubstitute);

        {
            Collection<Permission> perm = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), group.getId(), perm, ASystem.INSTANCE);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor1.getId(), perm, ASystem.INSTANCE);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor2.getId(), perm, ASystem.INSTANCE);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitute.getId(), perm, ASystem.INSTANCE);
        }
        {
            Collection<Permission> perm = Lists.newArrayList(ActorPermission.READ);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor1.getId(), perm, substitute);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitute.getId(), perm, actor1);
        }
        actor1User = testHelper.getAuthenticationService().authenticateByLoginPassword(nameActor1, pwdActor1);
        actor2SUser = testHelper.getAuthenticationService().authenticateByLoginPassword(nameActor2, pwdActor2);
        substituteUser = testHelper.getAuthenticationService().authenticateByLoginPassword(nameSubstitute, pwdSubstitute);

        substitutionCriteria_always = null;
        substitutionCriteria_always = testHelper.createSubstitutionCriteria(substitutionCriteria_always);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_NAME + ".par");
        testHelper.getDefinitionService().deployProcessDefinition(testHelper.getAdminUser(), parBytes, Lists.newArrayList("testProcess"));
        WfDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinition(testHelper.getAdminUser(), PROCESS_NAME);
        Collection<Permission> definitionPermission = Lists.newArrayList(DefinitionPermission.START_PROCESS);
        testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), actor1.getId(), definitionPermission, definition);

        batchPresentation = testHelper.getTaskBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        testHelper.getDefinitionService().undeployProcessDefinition(testHelper.getAdminUser(), PROCESS_NAME, null);
        testHelper.releaseResources();
        testHelper.removeSubstitutionCriteria(substitutionCriteria_always);
        super.tearDown();
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute assigns a task</li>
     * <li>User 1 tries to assign the task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    // rask:
    public void testAssignAssigned() throws Exception {
        WfTask[] actor1Tasks;
        WfTask[] actor2Tasks;
        WfTask[] substituteTasks;

        Substitution substitution1 = testHelper.createActorSubstitutor(actor1User, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1User, 0);
            actor2Tasks = checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null, null);
        {
            checkTaskList(actor1User, 1);
            checkTaskList(actor2SUser, 1);
            checkTaskList(substituteUser, 0);
        }
        testHelper.setActorStatus(actor1, false);
        testHelper.setActorStatus(actor2, false);
        {
            actor1Tasks = checkTaskList(actor1User, 1);
            actor2Tasks = checkTaskList(actor2SUser, 1);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        Actor actor = substituteUser.getActor();
        testHelper.getTaskService().assignTask(substituteUser, substituteTasks[0].getId(), substituteTasks[0].getOwner(), actor);
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        assertExceptionThrownOnAssign(actor1User, actor1Tasks[0]);
        assertExceptionThrownOnAssign(actor2SUser, actor2Tasks[0]);
        testHelper.getTaskService().completeTask(substituteUser, substituteTasks[0].getId(), new HashMap<String, Object>(), null);
        testHelper.removeCriteriaFromSubstitution(substitution1);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute executes a task</li>
     * <li>User 1 tries to assign the task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testAssignMoved() throws Exception {
        WfTask[] actor1Tasks;
        WfTask[] actor2Tasks;
        WfTask[] substituteTasks;

        Substitution substitution1 = testHelper.createActorSubstitutor(actor1User, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1User, 0);
            actor2Tasks = checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null, null);
        {
            checkTaskList(actor1User, 1);
            checkTaskList(actor2SUser, 1);
            checkTaskList(substituteUser, 0);
        }
        testHelper.setActorStatus(actor1, false);
        testHelper.setActorStatus(actor2, false);
        {
            actor1Tasks = checkTaskList(actor1User, 1);
            actor2Tasks = checkTaskList(actor2SUser, 1);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        testHelper.getTaskService().completeTask(substituteUser, substituteTasks[0].getId(), new HashMap<String, Object>(), null);
        {
            checkTaskList(actor1User, actor1Tasks[0]);
            checkTaskList(actor2SUser, actor2Tasks[0]);
            checkTaskList(substituteUser, substituteTasks[0]);
        }
        assertExceptionThrownOnAssign(actor1User, actor1Tasks[0]);
        assertExceptionThrownOnAssign(actor2SUser, actor2Tasks[0]);
        testHelper.removeCriteriaFromSubstitution(substitution1);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute assigns a task</li>
     * <li>User 1 tries to execute the task</li>
     * <li>User 2 tries to execute the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testMoveAssigned() throws Exception {
        WfTask[] actor1Tasks;
        WfTask[] actor2Tasks;
        WfTask[] substituteTasks;

        Substitution substitution1 = testHelper.createActorSubstitutor(actor1User, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1User, 0);
            actor2Tasks = checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 0);
        }
        testHelper.getExecutionService().startProcess(actor1User, PROCESS_NAME, null, null);
        {
            checkTaskList(actor1User, 1);
            checkTaskList(actor2SUser, 1);
            checkTaskList(substituteUser, 0);
        }
        testHelper.setActorStatus(actor1, false);
        testHelper.setActorStatus(actor2, false);
        {
            actor1Tasks = checkTaskList(actor1User, 1);
            actor2Tasks = checkTaskList(actor2SUser, 1);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        Actor actor = substituteUser.getActor();
        testHelper.getTaskService().assignTask(substituteUser, substituteTasks[0].getId(), substituteTasks[0].getOwner(), actor);
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        assertExceptionThrownOnExecute(actor1User, actor1Tasks[0]);
        assertExceptionThrownOnExecute(actor2SUser, actor2Tasks[0]);
        testHelper.removeCriteriaFromSubstitution(substitution1);
    }

    private void assertExceptionThrownOnExecute(User user, WfTask task) throws InternalApplicationException {
        try {
            testHelper.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>(), null);
            throw new InternalApplicationException("Exception not thrown. Actor shouldn't see assigned/executed task by another user...");
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        } catch (AuthorizationException e) {
            // task was already assigned/executed by another user
        } catch (TaskDoesNotExistException e) {
        } catch (ExecutorDoesNotExistException e) {
            throw new InternalApplicationException("ExecutorOutOfDateException exception thrown");
        } catch (ValidationException e) {
            throw new InternalApplicationException("ValidationException exception thrown");
        }
    }

    private void assertExceptionThrownOnAssign(User user, WfTask task) throws InternalApplicationException, ExecutorDoesNotExistException {
        try {
            Actor actor = user.getActor();
            testHelper.getTaskService().assignTask(user, task.getId(), task.getOwner(), actor);
            throw new InternalApplicationException("Exception TaskAlreadyAcceptedException not thrown");
        } catch (TaskDoesNotExistException e) {
            // TODO this is unexpected, fix me!!!
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
