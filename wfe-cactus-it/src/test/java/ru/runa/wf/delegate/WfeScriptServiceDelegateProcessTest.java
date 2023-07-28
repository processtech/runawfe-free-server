package ru.runa.wf.delegate;

import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfScriptServiceTestHelper;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

public class WfeScriptServiceDelegateProcessTest extends ServletTestCase {

    private static final String SET_ADD = "process_SetAdd_script.xml";
    private static final String ADD_SET = "process_AddSet_script.xml";
    private static final String REMOVE = "process_Remove_script.xml";

    private static final Group EMPLOYEE_GROUP = new Group("employee", null);

    private WfScriptServiceTestHelper h;

    private Executor employee;
    private WfProcess instanceStub1, instanceStub2;

    @Override
    protected void setUp() {
        h = new WfScriptServiceTestHelper(getClass().getName());
        employee = h.createGroupIfNotExist(EMPLOYEE_GROUP.getName(), EMPLOYEE_GROUP.getDescription());
        h.deployValidProcessDefinition();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        h = null;
    }

    public void testAddThenSetPermissions() {
        h.executeScript(ADD_SET);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        // group permission on process definition
        assertFalse("Check if 'DELETE' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.DELETE, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'START_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.START_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'READ' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.READ, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'UPDATE' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.UPDATE, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'READ_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.READ_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'CANCEL_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.CANCEL_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        instanceStub1 = h.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, h.getAdministrator());
        instanceStub2 = h.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, h.getAdministrator());

        // group permission on process instances
        assertTrue("Check if 'READ' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.READ, instanceStub1));

        assertTrue("Check if 'CANCEL' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.CANCEL, instanceStub1));

        assertTrue("Check if 'READ' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.READ, instanceStub2));

        assertTrue("Check if 'CANCEL' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.CANCEL, instanceStub2));

    }

    public void testSetThenAddPermissions() {
        h.executeScript(SET_ADD);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        // group permission on process definition
        assertTrue("Check if 'READ' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.READ, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'UPDATE' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.UPDATE, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'START_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.START_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'READ_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.READ_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'CANCEL_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.CANCEL_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        instanceStub1 = h.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, h.getAdministrator());
        instanceStub2 = h.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, h.getAdministrator());

        // group permission on process instances
        assertTrue("Check if 'READ' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.READ, instanceStub1));

        assertTrue("Check if 'CANCEL' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.CANCEL, instanceStub1));

        assertTrue("Check if 'READ' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.READ, instanceStub2));

        assertTrue("Check if 'CANCEL' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.CANCEL, instanceStub2));
    }

    public void testRemovePermissions() {
        h.executeScript(REMOVE);

        // ATTENTION! These tests are valid only for script file executed above. If that file is edited, the tests must be changed accordingly.

        // group permission on process definition
        assertFalse("Check if 'DELETE' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.DELETE, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'READ' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.READ, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'UPDATE' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.UPDATE, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'START_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.START_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertTrue("Check if 'READ_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.READ_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        assertFalse("Check if 'CANCEL_PROCESS' permission is given to employees on validProcess definition",
                h.hasOwnPermissionOnDefinition(employee, Permission.CANCEL_PROCESS, WfServiceTestHelper.VALID_PROCESS_NAME));

        instanceStub1 = h.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, h.getAdministrator());
        instanceStub2 = h.startProcessInstance(WfServiceTestHelper.VALID_PROCESS_NAME, h.getAdministrator());

        // group permission on process instances
        assertTrue("Check if 'READ' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.READ, instanceStub1));

        assertFalse("Check if 'CANCEL' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.CANCEL, instanceStub1));

        assertTrue("Check if 'READ' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.READ, instanceStub2));

        assertFalse("Check if 'CANCEL' permission is given to employee on validProcess instance",
                h.hasOwnPermission(employee, Permission.CANCEL, instanceStub2));
    }
}
