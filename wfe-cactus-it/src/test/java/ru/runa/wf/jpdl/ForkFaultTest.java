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
import com.google.common.collect.Maps;
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

/**
 * Created on 14.05.2005
 * 
 * @author Gritsenko_S
 */
public class ForkFaultTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ForkFaultTest.class);
    }

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        batchPresentation = h.getTaskBatchPresentation();

        h.deployValidProcessDefinition(WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_FILE_NAME);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(
                Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS),
                WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void test1() {
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME, null);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());
        List<String> expectedStateNames = Lists.newArrayList("state_2", "state_3");
        List<String> actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName());

        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        WfTask task = null;
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            }
        }
        assert (task != null);

        HashMap<String, Object> state2Variables = Maps.newHashMap();
        state2Variables.put("def_variable", "false");
        h.getTaskService().completeTask(h.getAuthorizedUser(), task.getId(), state2Variables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_3", tasks.get(0).getName());
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_4", tasks.get(0).getName());
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testFault1() {
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME, null);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());
        List<String> expectedStateNames = Lists.newArrayList("state_2", "state_3");
        List<String> actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName());

        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        WfTask task = null;
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            }
        }
        assert (task != null);

        HashMap<String, Object> state2Variables = Maps.newHashMap();
        state2Variables.put("def_variable", "true");
        h.getTaskService().completeTask(h.getAuthorizedUser(), task.getId(), state2Variables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());
        expectedStateNames = Lists.newArrayList("state_1", "state_3");
        actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        task = null;
        if (tasks.get(0).getName().equals("state_1")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_1")) {
                task = tasks.get(1);
            }
        }
        assert (task != null);

        h.getTaskService().completeTask(h.getAuthorizedUser(), task.getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 3, tasks.size());
        expectedStateNames = Lists.newArrayList("state_2", "state_3", "state_3");
        actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName(), tasks.get(2).getName());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            } else {
                task = tasks.get(2);
            }
        }

        state2Variables = new HashMap<>();
        state2Variables.put("def_variable", "false");
        h.getTaskService().completeTask(h.getAuthorizedUser(), task.getId(), state2Variables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());

        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(1).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_4", tasks.get(0).getName());
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }
}
