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

import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wf.service.WfScriptServiceTestHelper;
import ru.runa.wf.service.WfServiceTestHelper;

public class WfeScriptServiceDelegateProcessTest extends ServletTestCase {

    private static final String SET_ADD = "process_SetAdd_script.xml";
    private static final String ADD_SET = "process_AddSet_script.xml";
    private static final String REMOVE = "process_Remove_script.xml";

    private static final Group EMPLOYEE_GROUP = new Group("employee", null);
    private Executor employee = null;
    private WfProcess instanceStub1, instanceStub2;

    private WfScriptServiceTestHelper helper = null;

    protected void setUp() throws Exception {
        helper = new WfScriptServiceTestHelper(getClass().getName());
        employee = helper.createGroupIfNotExist(EMPLOYEE_GROUP.getName(), EMPLOYEE_GROUP.getDescription());
        helper.deployValidProcessDefinition();
        instanceStub1 = helper.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, employee);
        instanceStub2 = helper.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, employee);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        helper = null;
        super.tearDown();
    }

    public void testAddThenSetPermissions() throws Exception {

        //script with operations is parsed and executed here
        helper.executeScript(ADD_SET);

        /** This checks are valid only for one paticular given script file. 
         *  If this script file is edited the following test method must be changed 
         *  in corresponding way
         */

        //group permission on process definition
        assertTrue("Check if 'read' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                Permission.READ, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'update_permissions' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(Permission.UPDATE_PERMISSIONS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'redeploy_definition' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(DefinitionPermission.REDEPLOY_DEFINITION, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'undeploy_definition' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(DefinitionPermission.UNDEPLOY_DEFINITION, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'start_process' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.START_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'read_instance' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.READ_STARTED_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'cancel_instance' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.CANCEL_STARTED_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        //group permission on process instances
        assertTrue("Check if 'read' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub1, employee,
                Permission.READ));

        assertTrue("Check if 'update_permissions' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(
                instanceStub1, employee, Permission.UPDATE_PERMISSIONS));

        assertTrue("Check if 'cancel_instance' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub1,
                employee, ProcessPermission.CANCEL_PROCESS));

        assertTrue("Check if 'read' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub2, employee,
                Permission.READ));

        assertTrue("Check if 'update_permissions' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(
                instanceStub2, employee, Permission.UPDATE_PERMISSIONS));

        assertTrue("Check if 'cancel_instance' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub2,
                employee, ProcessPermission.CANCEL_PROCESS));

    }

    public void testSetThenAddPermissions() throws Exception {

        //script with operations is parsed and executed here
        helper.executeScript(SET_ADD);

        /** This checks are valid only for one paticular given script file. 
         *  If this script file is edited the following test method must be changed 
         *  in corresponding way
         */
        //group permission on process definition
        assertTrue("Check if 'read' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                Permission.READ, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'update_permissions' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(Permission.UPDATE_PERMISSIONS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'redeploy_definition' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(DefinitionPermission.REDEPLOY_DEFINITION, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'undeploy_definition' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(DefinitionPermission.UNDEPLOY_DEFINITION, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'start_process' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.START_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'read_instance' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.READ_STARTED_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'cancel_instance' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.CANCEL_STARTED_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        //group permission on process instances
        assertTrue("Check if 'read' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub1, employee,
                Permission.READ));

        assertTrue("Check if 'update_permissions' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(
                instanceStub1, employee, Permission.UPDATE_PERMISSIONS));

        assertTrue("Check if 'cancel_instance' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub1,
                employee, ProcessPermission.CANCEL_PROCESS));

        assertTrue("Check if 'read' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub2, employee,
                Permission.READ));

        assertTrue("Check if 'update_permissions' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(
                instanceStub2, employee, Permission.UPDATE_PERMISSIONS));

        assertTrue("Check if 'cancel_instance' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub2,
                employee, ProcessPermission.CANCEL_PROCESS));

    }

    public void testRemovePermissions() throws Exception {
        //script with operations is parsed and executed here
        helper.executeScript(REMOVE);

        /** This checks are valid only for one paticular given script file. 
         *  If this script file is edited the following test method must be changed 
         *  in corresponding way
         */
        //group permission on process definition
        assertTrue("Check if 'read' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                Permission.READ, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'update_permissions' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(Permission.UPDATE_PERMISSIONS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'redeploy_definition' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(DefinitionPermission.REDEPLOY_DEFINITION, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'undeploy_definition' permission is given to employees on validProcess definition", helper
                .isAllowedToExecutorOnDefinition(DefinitionPermission.UNDEPLOY_DEFINITION, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'start_process' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.START_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'read_instance' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.READ_STARTED_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'cancel_instance' permission is given to employees on validProcess definition", helper.isAllowedToExecutorOnDefinition(
                DefinitionPermission.CANCEL_STARTED_PROCESS, employee, WfServiceTestHelper.VALID_PROCESS_NAME));

        //group permission on process instances
        assertTrue("Check if 'read' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub1, employee,
                Permission.READ));

        assertTrue("Check if 'update_permissions' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(
                instanceStub1, employee, Permission.UPDATE_PERMISSIONS));

        assertTrue("Check if 'cancel_instance' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub1,
                employee, ProcessPermission.CANCEL_PROCESS));

        assertTrue("Check if 'read' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub2, employee,
                Permission.READ));

        assertTrue  ("Check if 'update_permissions' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(
                instanceStub2, employee, Permission.UPDATE_PERMISSIONS));

        assertTrue("Check if 'cancel_instance' permission is given to employee on validProcess instance", helper.isAllowedToExecutor(instanceStub2,
                employee, ProcessPermission.CANCEL_PROCESS));

    }
}
