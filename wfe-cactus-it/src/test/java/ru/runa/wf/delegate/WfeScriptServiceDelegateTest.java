/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.delegate;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.wfe.user.Actor;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wf.service.WfScriptServiceTestHelper;

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

    private WfScriptServiceTestHelper helper = null;

    protected void setUp() throws Exception {
        helper = new WfScriptServiceTestHelper(getClass().getName());
        super.setUp();
    }

    protected void tearDown() throws Exception {
        removeExecutorsIfExist();
        helper.releaseResources();
        helper = null;
        super.tearDown();
    }

    public void testCreateActorGroup() throws Exception {

        // check if group and actor exist and remove if so
        removeExecutorsIfExist();

        // script with operations is parsed and executed here
        helper.executeScript(EXECUTOR_CREATE);

        /**
         * This checks are valid only for one paticular given script file. If this script file is edited the following test method must be changed in
         * corresponding way
         */
        for (int i = 0; i < EXECUTORS.length; i++) {
            Executor actual = helper.getExecutor(EXECUTORS[i].getName());
            assertTrue("Executors creation name check" + actual.getName(), helper.areExecutorsWeaklyEqual(actual, EXECUTORS[i]));
        }

        // check if password is correct
        assertTrue("Check if tin password is correct", helper.isPasswordCorrect(TIN_ACTOR.getName(), TIN_ACTOR_PASSWORD));

        // check if actors are added to groups correctly
        Group employeeGroup = (Group) helper.getExecutor(EMPLOYEE_GROUP.getName());
        for (int i = 0; i < EMPLOYEE_GROUP_EXECUTORS.length; i++) {
            Executor actual = helper.getExecutor(EMPLOYEE_GROUP_EXECUTORS[i].getName());
            assertTrue("Employee group membership check: " + actual.getName() + " is in " + employeeGroup.getName(),
                    helper.isExecutorInGroup(actual, employeeGroup));
        }

        Group bossGroup = (Group) helper.getExecutor(BOSS_GROUP.getName());
        for (int i = 0; i < BOSS_GROUP_EXECUTORS.length; i++) {
            Executor actual = helper.getExecutor(BOSS_GROUP_EXECUTORS[i].getName());
            assertTrue("Boss group membership check: " + actual.getName() + " is in " + bossGroup.getName(),
                    helper.isExecutorInGroup(actual, bossGroup));
        }

    }

    public void testAddThenSetPermissions() throws Exception {
        // check if group and actor exist and remove if so
        removeExecutorsIfExist();

        // script with operations is parsed and executed here
        helper.executeScript(ADD_SET);

        /**
         * This checks are valid only for one paticular given script file. If this script file is edited the following test method must be changed in
         * corresponding way
         */
        // executors are created by script
        Executor toto = helper.getExecutor(TOTO_ACTOR.getName());
        Executor dorothy = helper.getExecutor(DOROTHY_ACTOR.getName());
        Executor employee = helper.getExecutor(EMPLOYEE_GROUP.getName());
        Executor tin = helper.getExecutor(TIN_ACTOR.getName());

        // actor permissions on actors
        assertTrue("Check if 'read' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.READ));

        assertFalse("Check if 'update' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.UPDATE));

        // actor permissions on groups
        assertFalse("Check if 'list' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.LIST));

        assertFalse("Check if 'read' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.READ));

        assertFalse("Check if 'view_tasks' permission is given to dorothy on employee",
                helper.isAllowedToExecutor(employee, dorothy, Permission.VIEW_TASKS));

        assertTrue("Check if 'update' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.UPDATE));

        assertTrue("Check if 'update_status' permission is given to dorothy on employee",
                helper.isAllowedToExecutor(employee, dorothy, Permission.UPDATE_STATUS));

        assertTrue("Check if 'delete' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.DELETE));

        // group permission on groups
        assertTrue("Check if 'read' permission is given to employee on employee", helper.isAllowedToExecutor(employee, employee, Permission.READ));

        assertFalse("Check if 'list' permission is given to employees on employee", helper.isAllowedToExecutor(employee, employee, Permission.LIST));

        assertFalse("Check if 'update' permission is given to employees on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.UPDATE));

        assertFalse("Check if 'delete' permission is given to employee on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.DELETE));

        // group permission on actor
        assertFalse("Check if 'read' permission is given to employee on tin", helper.isAllowedToExecutor(tin, employee, Permission.READ));

        assertTrue("Check if 'update' permission is given to employee on tin", helper.isAllowedToExecutor(tin, employee, Permission.UPDATE));

        // group permission on Executors
        assertTrue("Check if 'update' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.UPDATE));

        assertTrue("Check if 'create' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.CREATE));

        assertFalse("Check if 'login' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.LOGIN));

        // group permission on Definitions
        assertFalse("Check if 'read' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.READ));

        assertFalse("Check if 'list' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.LIST));

        assertTrue("Check if 'create' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.CREATE));

        assertTrue("Check if 'update' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.UPDATE));

        assertTrue("Check if 'start' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.START));

        assertTrue("Check if 'read_process' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.READ_PROCESS));

        assertTrue("Check if 'cancel_process' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.CANCEL_PROCESS));
    }

    public void testSetThenAddPermissions() throws Exception {
        // check if group and actor exist and remove if so
        removeExecutorsIfExist();

        // script with operations is parsed and executed here
        helper.executeScript(SET_ADD);

        /**
         * This checks are valid only for one paticular given script file. If this script file is edited the following test method must be changed in
         * corresponding way
         */
        Executor toto = helper.getExecutor(TOTO_ACTOR.getName());
        Executor dorothy = helper.getExecutor(DOROTHY_ACTOR.getName());
        Executor employee = helper.getExecutor(EMPLOYEE_GROUP.getName());
        Executor tin = helper.getExecutor(TIN_ACTOR.getName());

        // actor permissions on actors
        assertTrue("Check if 'list' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.LIST));

        assertTrue("Check if 'read' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.READ));

        assertTrue("Check if 'update' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.UPDATE));

        // actor permissions on groups
        assertTrue("Check if 'list' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.LIST));

        assertTrue("Check if 'read' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.READ));

        assertTrue("Check if 'update' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.UPDATE));

        assertTrue("Check if 'delete' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.DELETE));

        // group permission on groups
        assertTrue("Check if 'list' permission is given to employee on employee", helper.isAllowedToExecutor(employee, employee, Permission.LIST));

        assertTrue("Check if 'read' permission is given to employees on employee", helper.isAllowedToExecutor(employee, employee, Permission.READ));

        assertTrue("Check if 'update' permission is given to employees on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.UPDATE));

        assertTrue("Check if 'delete' permission is given to employee on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.DELETE));

        // group permission on actor
        assertTrue("Check if 'read' permission is given to employee on tin", helper.isAllowedToExecutor(tin, employee, Permission.READ));

        assertTrue("Check if 'update' permission is given to employee on tin", helper.isAllowedToExecutor(tin, employee, Permission.UPDATE));

        // group permission on Executors
        assertTrue("Check if 'read' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.READ));

        assertTrue("Check if 'update' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.UPDATE));

        assertTrue("Check if 'login' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.LOGIN));

        assertTrue("Check if 'create' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.CREATE));

        // group permission on Definitions
        assertTrue("Check if 'read' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.READ));

        assertTrue("Check if 'create' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.CREATE));

        assertTrue("Check if 'update' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.UPDATE));

        assertTrue("Check if 'start' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.START));

        assertTrue("Check if 'read_process' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.READ_PROCESS));

        assertTrue("Check if 'cancel_process' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.CANCEL_PROCESS));
    }

    public void testRemove() throws Exception {
        // check if group and actor exist and remove if so
        removeExecutorsIfExist();

        // script with operations is parsed and executed here
        helper.executeScript(REMOVE);

        /**
         * This checks are valid only for one paticular given script file. If this script file is edited the following test method must be changed in
         * corresponding way
         */
        Executor tin = helper.getExecutor(TIN_ACTOR.getName());
        Executor toto = helper.getExecutor(TOTO_ACTOR.getName());
        Executor dorothy = helper.getExecutor(DOROTHY_ACTOR.getName());
        Executor employee = helper.getExecutor(EMPLOYEE_GROUP.getName());

        // assert that tin is still in employee group
        assertTrue("Tin is in employee group", helper.isExecutorInGroup(tin, (Group) employee));

        // assert that dorothy is not in employee group
        assertFalse("dorothy is not in employee group", helper.isExecutorInGroup(dorothy, (Group) employee));

        // actor permissions on actors
        assertFalse("Dorothy's 'list' permission on toto is revoked", helper.isAllowedToExecutor(toto, dorothy, Permission.LIST));

        assertTrue("Check if 'read' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.READ));

        assertTrue("Check if 'update' permission is given to dorothy on toto", helper.isAllowedToExecutor(toto, dorothy, Permission.UPDATE));

        // actor permissions on groups
        assertFalse("Dorothy's 'update_status' permission on employee is revoked",
                helper.isAllowedToExecutor(employee, dorothy, Permission.UPDATE_STATUS));

        assertTrue("Check if 'read' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.READ));

        assertTrue("Check if 'update' permission is given to dorothy on employee", helper.isAllowedToExecutor(employee, dorothy, Permission.UPDATE));

        // group permissions on group
        assertTrue("Check if 'read' permission is given to employee on employee", helper.isAllowedToExecutor(employee, employee, Permission.READ));

        assertTrue("Check if 'delete' permission is given to employees on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.DELETE));

        assertTrue("Check if 'view_tasks' permission is given to employees on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.VIEW_TASKS));

        assertFalse("Check if 'list' permission is given to employees on employee", helper.isAllowedToExecutor(employee, employee, Permission.LIST));

        assertFalse("Check if 'update' permission is given to employee on employee",
                helper.isAllowedToExecutor(employee, employee, Permission.DELETE));

        // group permission on actor
        assertFalse("Check if 'update' permission is given to employee on tin", helper.isAllowedToExecutor(tin, employee, Permission.UPDATE));

        assertTrue("Check if 'read' permission is given to employee on tin", helper.isAllowedToExecutor(tin, employee, Permission.READ));

        // group permission on Executors
        assertTrue("Check if 'read' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.READ));

        assertTrue("Check if 'login' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.LOGIN));

        assertTrue("Check if 'create' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.CREATE));

        assertFalse("Check if 'update' permission is given to employee on Executors",
                helper.isAllowedToExecutor(SecuredSingleton.EXECUTORS, employee, Permission.UPDATE));

        // group permission on Definitions
        assertTrue("Check if 'create' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.CREATE));

        assertTrue("Check if 'update' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.UPDATE));

        assertTrue("Check if 'start' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.START));

        assertFalse("Check if 'read' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.READ));

        assertFalse("Check if 'read_process' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.READ_PROCESS));

        assertFalse("Check if 'cancel_process' permission is given to employee on Definitions",
                helper.isAllowedToExecutor(SecuredSingleton.DEFINITIONS, employee, Permission.CANCEL_PROCESS));

        try {
            helper.getExecutor(GLUCH_ACTOR.getName());
            fail("Deleted executor present");
        } catch (ExecutorDoesNotExistException e) {

        }
    }

    private void removeExecutorsIfExist() throws AuthorizationException, AuthenticationException {
        for (int i = 0; i < EXECUTORS.length; i++) {
            helper.removeExecutorIfExists(EXECUTORS[i]);
        }
        helper.removeExecutorIfExists(EMPLOYEE_GROUP);
        helper.removeExecutorIfExists(BOSS_GROUP);
    }

}
