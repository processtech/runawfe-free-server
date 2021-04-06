package ru.runa.wf.delegate;

import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfScriptServiceTestHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

/**
 * Edited on 15.04.2006
 * 
 * @author Beresina N.
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */

public class WfeScriptServiceDelegateTest extends ServletTestCase {

    private static final String EXECUTOR_CREATE = "executor_Create_script.xml";
    private static final String SET_ADD = "executor_SetAdd_script.xml";
    private static final String ADD_SET = "executor_AddSet_script.xml";
    private static final String REMOVE = "executor_Remove_script.xml";

    private static final Group EMPLOYEE_GROUP = new Group("employee", null);
    private static final Group BOSS_GROUP = new Group("boss", null);

    private static final Actor DOROTHY_ACTOR = new Actor("dorothy", null, "Dorothy Gale");
    private static final Actor TOTO_ACTOR = new Actor("toto", "Dorothy's dog", null);
    private static final Actor LION_ACTOR = new Actor("lion", null, "The Cowardly Lion");
    private static final Actor SCARECROW_ACTOR = new Actor("scarecrow", null, "The Scarecrow");
    private static final Actor TIN_ACTOR = new Actor("tin", null, "The Tin Man");
    private static final Actor GLUCH_ACTOR = new Actor("gulch", "Someone we will create and delete in this script", "The Wicked Witch of the West");

    private static final String TIN_ACTOR_PASSWORD = "secretword";

    private static final Executor[] EXECUTORS = { EMPLOYEE_GROUP, BOSS_GROUP, DOROTHY_ACTOR, TOTO_ACTOR, LION_ACTOR, SCARECROW_ACTOR, TIN_ACTOR,
            GLUCH_ACTOR };
    private static final Executor[] EMPLOYEE_GROUP_EXECUTORS = { DOROTHY_ACTOR, LION_ACTOR, SCARECROW_ACTOR, TIN_ACTOR, GLUCH_ACTOR };
    private static final Executor[] BOSS_GROUP_EXECUTORS = { DOROTHY_ACTOR };

    private WfScriptServiceTestHelper h = null;

    @Override
    protected void setUp() {
        h = new WfScriptServiceTestHelper(getClass().getName());
    }

    @Override
    protected void tearDown() {
        removeExecutorsIfExist();
        h.releaseResources();
        h = null;
    }

    public void testCreateActorGroup() {
        removeExecutorsIfExist();
        h.executeScript(EXECUTOR_CREATE);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        for (Executor e : EXECUTORS) {
            Executor actual = h.getExecutor(e.getName());
            assertTrue("Executors creation name check" + actual.getName(), h.areExecutorsWeaklyEqual(actual, e));
        }

        // check if password is correct
        assertTrue("Check if tin password is correct", h.isPasswordCorrect(TIN_ACTOR.getName(), TIN_ACTOR_PASSWORD));

        // check if actors are added to groups correctly
        Group employeeGroup = (Group) h.getExecutor(EMPLOYEE_GROUP.getName());
        for (Executor e : EMPLOYEE_GROUP_EXECUTORS) {
            Executor actual = h.getExecutor(e.getName());
            assertTrue("Employee group membership check: " + actual.getName() + " is in " + employeeGroup.getName(),
                    h.isExecutorInGroup(actual, employeeGroup));
        }

        Group bossGroup = (Group) h.getExecutor(BOSS_GROUP.getName());
        for (Executor e : BOSS_GROUP_EXECUTORS) {
            Executor actual = h.getExecutor(e.getName());
            assertTrue("Boss group membership check: " + actual.getName() + " is in " + bossGroup.getName(), h.isExecutorInGroup(actual, bossGroup));
        }

    }

    public void testAddThenSetPermissions() {
        removeExecutorsIfExist();
        h.executeScript(ADD_SET);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        // executors are created by script
        Executor toto = h.getExecutor(TOTO_ACTOR.getName());
        Executor dorothy = h.getExecutor(DOROTHY_ACTOR.getName());
        Executor employee = h.getExecutor(EMPLOYEE_GROUP.getName());
        Executor tin = h.getExecutor(TIN_ACTOR.getName());

        // actor permissions on actors
        assertTrue("Check if 'READ' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.READ, toto));

        assertFalse("Check if 'UPDATE' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.UPDATE, toto));

        // actor permissions on groups
        assertTrue("Check if 'READ' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.READ, employee));

        assertFalse("Check if 'UPDATE' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.UPDATE, employee));

        assertFalse("Check if 'UPDATE_PERMISSIONS' permission is given to dorothy on employee",
                h.hasOwnPermission(dorothy, Permission.UPDATE_PERMISSIONS, employee));

        assertTrue("Check if 'VIEW_TASKS' permission is given to dorothy on employee",
                h.hasOwnPermission(dorothy, Permission.VIEW_TASKS, employee));

        // group permission on groups
        assertTrue("Check if 'READ' permission is given to employee on employee", h.hasOwnPermission(employee, Permission.READ, employee));

        assertFalse("Check if 'UPDATE' permission is given to employees on employee", h.hasOwnPermission(employee, Permission.UPDATE, employee));

        assertFalse("Check if 'VIEW_TASKS' permission is given to employee on employee",
                h.hasOwnPermission(employee, Permission.VIEW_TASKS, employee));

        // group permission on actor
        assertFalse("Check if 'READ' permission is given to employee on tin", h.hasOwnPermission(employee, Permission.READ, tin));

        assertTrue("Check if 'UPDATE' permission is given to employee on tin", h.hasOwnPermission(employee, Permission.UPDATE, tin));

        // group permission on singleton
        assertFalse("Check if 'LOGIN' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.LOGIN, SecuredSingleton.SYSTEM));

        assertFalse("Check if 'CREATE_EXECUTOR' permission is given to employee on Definitions",
                h.hasOwnPermission(employee, Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM));

        assertFalse("Check if 'CREATE_DEFINITION' permission is given to employee on Definitions",
                h.hasOwnPermission(employee, Permission.CREATE_DEFINITION, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'READ' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.READ, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'VIEW_LOGS' permission is given to employee on Definitions",
                h.hasOwnPermission(employee, Permission.VIEW_LOGS, SecuredSingleton.SYSTEM));

        assertFalse("Check if 'CHANGE_SELF_PASSWORD' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.CHANGE_SELF_PASSWORD, SecuredSingleton.SYSTEM));
    }

    public void testSetThenAddPermissions() {
        removeExecutorsIfExist();
        h.executeScript(SET_ADD);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        Executor toto = h.getExecutor(TOTO_ACTOR.getName());
        Executor dorothy = h.getExecutor(DOROTHY_ACTOR.getName());
        Executor employee = h.getExecutor(EMPLOYEE_GROUP.getName());
        Executor tin = h.getExecutor(TIN_ACTOR.getName());

        // actor permissions on actors
        assertTrue("Check if 'READ' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.READ, toto));

        assertTrue("Check if 'VIEW_TASKS' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.VIEW_TASKS, toto));

        assertTrue("Check if 'UPDATE' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.UPDATE, toto));

        // actor permissions on groups
        assertTrue("Check if 'READ' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.READ, employee));

        assertTrue("Check if 'VIEW_TASKS' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.VIEW_TASKS, employee));

        assertTrue("Check if 'UPDATE' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.UPDATE, employee));

        assertTrue("Check if 'UPDATE_PERMISSIONS' permission is given to dorothy on employee",
                h.hasOwnPermission(dorothy, Permission.UPDATE_PERMISSIONS, employee));

        // group permission on groups
        assertTrue("Check if 'READ' permission is given to employees on employee", h.hasOwnPermission(employee, Permission.READ, employee));

        assertTrue("Check if 'VIEW_TASKS' permission is given to employee on employee",
                h.hasOwnPermission(employee, Permission.VIEW_TASKS, employee));

        assertTrue("Check if 'UPDATE' permission is given to employees on employee", h.hasOwnPermission(employee, Permission.UPDATE, employee));

        assertTrue("Check if 'UPDATE_PERMISSIONS' permission is given to employee on employee",
                h.hasOwnPermission(employee, Permission.UPDATE_PERMISSIONS, employee));

        // group permission on actor
        assertTrue("Check if 'READ' permission is given to employee on tin", h.hasOwnPermission(employee, Permission.READ, tin));

        assertTrue("Check if 'UPDATE' permission is given to employee on tin", h.hasOwnPermission(employee, Permission.UPDATE, tin));

        // group permission on SYSTEM singleton
        assertTrue("Check if 'LOGIN' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.LOGIN, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'READ' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.READ, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'CREATE_DEFINITION' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.CREATE_DEFINITION, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'CREATE_EXECUTOR' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM));
    }

    public void testRemove() {
        removeExecutorsIfExist();
        h.executeScript(REMOVE);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        val tin = h.getExecutor(TIN_ACTOR.getName());
        val toto = h.getExecutor(TOTO_ACTOR.getName());
        val dorothy = h.getExecutor(DOROTHY_ACTOR.getName());
        val employee = (Group) h.getExecutor(EMPLOYEE_GROUP.getName());

        assertTrue("Tin is in employee group", h.isExecutorInGroup(tin, employee));
        assertFalse("dorothy is not in employee group", h.isExecutorInGroup(dorothy, employee));

        // actor permissions on actors
        assertFalse("Dorothy's 'VIEW_TASKS' permission on toto is revoked", h.hasOwnPermission(dorothy, Permission.VIEW_TASKS, toto));

        assertTrue("Check if 'READ' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.READ, toto));

        assertTrue("Check if 'UPDATE' permission is given to dorothy on toto", h.hasOwnPermission(dorothy, Permission.UPDATE, toto));

        // actor permissions on groups
        assertTrue("Check if 'READ' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.READ, employee));

        assertTrue("Check if 'UPDATE' permission is given to dorothy on employee", h.hasOwnPermission(dorothy, Permission.UPDATE, employee));

        assertFalse("Dorothy's 'UPDATE_PERMISSIONS' permission on employee is revoked",
                h.hasOwnPermission(dorothy, Permission.UPDATE_PERMISSIONS, employee));

        // group permissions on group
        assertTrue("Check if 'READ' permission is given to employee on employee", h.hasOwnPermission(employee, Permission.READ, employee));

        assertFalse("Check if 'UPDATE' permission is given to employee on employee",
                h.hasOwnPermission(employee, Permission.UPDATE, employee));

        assertFalse("Check if 'UPDATE_PERMISSIONS' permission is given to employee on employee",
                h.hasOwnPermission(employee, Permission.UPDATE_PERMISSIONS, employee));

        assertTrue("Check if 'VIEW_TASKS' permission is given to employees on employee",
                h.hasOwnPermission(employee, Permission.VIEW_TASKS, employee));

        // group permission on actor
        assertTrue("Check if 'READ' permission is given to employee on tin", h.hasOwnPermission(employee, Permission.READ, tin));

        assertFalse("Check if 'UPDATE' permission is given to employee on tin", h.hasOwnPermission(employee, Permission.UPDATE, tin));

        assertTrue("Check if 'UPDATE_ACTOR_STATUS' permission is given to employee on tin",
                h.hasOwnPermission(employee, Permission.UPDATE_ACTOR_STATUS, tin));

        // group permission on SYSTEM singleton
        assertFalse("Check if 'LOGIN' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.LOGIN, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'READ' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.READ, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'CREATE_DEFINITION' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.CREATE_DEFINITION, SecuredSingleton.SYSTEM));

        assertTrue("Check if 'CREATE_EXECUTOR' permission is given to employee on Executors",
                h.hasOwnPermission(employee, Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM));

        try {
            h.getExecutor(GLUCH_ACTOR.getName());
            fail("Deleted executor present");
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }

    private void removeExecutorsIfExist() {
        for (Executor e : EXECUTORS) {
            h.removeExecutorIfExists(e);
        }
        h.removeExecutorIfExists(EMPLOYEE_GROUP);
        h.removeExecutorIfExists(BOSS_GROUP);
    }
}
