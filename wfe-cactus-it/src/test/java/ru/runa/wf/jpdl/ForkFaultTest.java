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
import com.google.common.collect.Maps;

/**
 * Created on 14.05.2005
 * 
 * @author Gritsenko_S
 */
public class ForkFaultTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ForkFaultTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);

        batchPresentation = th.getTaskBatchPresentation();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void test1() throws Exception {
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME, null);

        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
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
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), task.getId(), state2Variables);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_3", tasks.get(0).getName());
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_4", tasks.get(0).getName());
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testFault1() throws Exception {
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME, null);

        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
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
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), task.getId(), state2Variables);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
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

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), task.getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 3, tasks.size());
        expectedStateNames = Lists.newArrayList("state_2", "state_3", "state_3");
        actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName(), tasks.get(2).getName());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        task = null;
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            } else {
                task = tasks.get(2);
            }
        }

        state2Variables = new HashMap<String, Object>();
        state2Variables.put("def_variable", "false");
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), task.getId(), state2Variables);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>());
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(1).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_4", tasks.get(0).getName());
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }
}
