package ru.runa.wf.jpdl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Lists;

/**
 * Created on 16.05.2005
 * 
 * @author Gritsenko_S
 */
public class Fork2Test extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private BatchPresentation batchPresentation;

    private HashMap<String, Object> startVariables;

    public static Test suite() {
        return new TestSuite(Fork2Test.class);
    }

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_2_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME);

        batchPresentation = th.getTaskBatchPresentation();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testVariant1() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable", "false");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, startVariables);

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
        assertEquals("task name differs from expected", "state_5", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_7", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(1).getId(), new HashMap<String, Object>(), null);

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

    public void testVariant2() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable", "true");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, startVariables);

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
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        WfTask task = null;
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
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        task = null;
        if (erpTasks.get(0).getName().equals("state_7")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_7")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

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
        startVariables.put("def_variable", "true");
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, startVariables);

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
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        WfTask task = null;
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
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        th.getTaskService().completeTask(th.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = th.getTaskService().getMyTasks(th.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", th.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", th.getErpOperator(), erpTasks.get(1).getOwner());

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

        th.getTaskService().completeTask(th.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        th.getTaskService().completeTask(th.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

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
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", th.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

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
