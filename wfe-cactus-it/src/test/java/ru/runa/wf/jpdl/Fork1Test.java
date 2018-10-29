package ru.runa.wf.jpdl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Lists;

/**
 * Created on 14.05.2005
 * 
 * @author Gritsenko_S
 */
public class Fork1Test extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private BatchPresentation batchPresentation;

    private HashMap<String, Object> startVariables;

    public static Test suite() {
        return new TestSuite(Fork1Test.class);
    }

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_1_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ,
                DefinitionPermission.READ_STARTED_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME);

        batchPresentation = th.getTaskBatchPresentation();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testVariant1() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "true");
        startVariables.put("def_variable2", "false");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        WfTask task = null;
        if (erpTasks.get(0).getName().equals("state_2")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_2")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        task = null;
        if (erpTasks.get(0).getName().equals("state_4")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_4")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant2() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "false");
        startVariables.put("def_variable2", "false");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_5", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant3() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "false");
        startVariables.put("def_variable2", "true");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, hrTasks.size());
        String[] expectedStateNames = { "state_3", "state_5" };
        String[] actualStateNames = { hrTasks.get(0).getName(), hrTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(1).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        WfTask task = null;
        if (hrTasks.get(0).getName().equals("state_5")) {
            task = hrTasks.get(0);
        } else {
            if (hrTasks.get(1).getName().equals("state_5")) {
                task = hrTasks.get(1);
            }
        }
        assert (task != null);

        th.getTaskService().completeTask(th.getHrOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant4() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "true");
        startVariables.put("def_variable2", "true");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        WfTask task = null;
        if (erpTasks.get(0).getName().equals("state_2")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_2")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, hrTasks.size());
        expectedStateNames = new String[] { "state_3", "state_3" };
        actualStateNames = new String[] { hrTasks.get(0).getName(), hrTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_4", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), null, null);
        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(1).getId(), null, null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_4", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant4async() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "true");
        startVariables.put("def_variable2", "true");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        WfTask task = null;
        if (erpTasks.get(0).getName().equals("state_2")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_2")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, hrTasks.size());
        expectedStateNames = new String[] { "state_3", "state_3" };
        actualStateNames = new String[] { hrTasks.get(0).getName(), hrTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_4", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        WfTask erpTask2;
        { // Ok, we want to complete tasks from different token. This tasks id
          // must have id of task between them
            long[] ids = new long[] { erpTasks.get(0).getId(), erpTasks.get(1).getId(), hrTasks.get(0).getId(), hrTasks.get(1).getId() };
            Arrays.sort(ids);
            int idx = Arrays.binarySearch(ids, hrTasks.get(0).getId());

            if (idx > 1) { // search at .get(0) and .get(1)
                if ((erpTasks.get(0).getId() == ids[0]) || (erpTasks.get(0).getId() == ids[1])) {
                    erpTask2 = erpTasks.get(0);
                } else {
                    erpTask2 = erpTasks.get(1);
                }
            } else { // search at [2] and [3]
                if ((erpTasks.get(0).getId() == ids[2]) || (erpTasks.get(0).getId() == ids[3])) {
                    erpTask2 = erpTasks.get(0);
                } else {
                    erpTask2 = erpTasks.get(1);
                }
            }
        }

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTask2.getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, performerTasks.size());
        expectedStateNames = new String[] { "state_6", "state_6" };
        actualStateNames = new String[] { performerTasks.get(0).getName(), performerTasks.get(1).getName() };
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(1).getOwner());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }
}
