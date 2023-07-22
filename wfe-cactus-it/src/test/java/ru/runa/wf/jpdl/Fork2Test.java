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
package ru.runa.wf.jpdl;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 16.05.2005
 * 
 * @author Gritsenko_S
 */
public class Fork2Test extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(Fork2Test.class);
    }

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        batchPresentation = h.getTaskBatchPresentation();

        h.deployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_2_PROCESS_FILE_NAME);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(
                Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS),
                WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void testVariant1() {
        val vars = new HashMap<String, Object>();
        vars.put("def_variable", "false");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, vars);

        List<WfTask> hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_5", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_7", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(0).getId(), null);
        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(1).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", h.getAuthorizedActor(), performerTasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), performerTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", h.getAuthorizedActor(), performerTasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), performerTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant2() {
        val vars = new HashMap<String, Object>();
        vars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, vars);

        List<WfTask> hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
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
        h.getTaskService().completeTask(h.getErpOperatorUser(), task.getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
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

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);
        h.getTaskService().completeTask(h.getErpOperatorUser(), task.getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", h.getAuthorizedActor(), performerTasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), performerTasks.get(0).getId(), null);

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", h.getAuthorizedActor(), performerTasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), performerTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant3() {
        val vars = new HashMap<String, Object>();
        vars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, vars);

        List<WfTask> hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
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

        h.getTaskService().completeTask(h.getErpOperatorUser(), task.getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", h.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
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

        h.getTaskService().completeTask(h.getHrOperatorUser(), hrTasks.get(0).getId(), null);
        h.getTaskService().completeTask(h.getErpOperatorUser(), task.getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", h.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        h.getTaskService().completeTask(h.getErpOperatorUser(), erpTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", h.getAuthorizedActor(), performerTasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), performerTasks.get(0).getId(), null);

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", h.getAuthorizedActor(), performerTasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), performerTasks.get(0).getId(), null);

        hrTasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }
}
