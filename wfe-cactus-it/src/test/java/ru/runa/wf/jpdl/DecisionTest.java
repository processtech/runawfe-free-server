package ru.runa.wf.jpdl;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
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
public class DecisionTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(DecisionTest.class);
    }

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        batchPresentation = h.getTaskBatchPresentation();

        h.deployValidProcessDefinition(WfServiceTestHelper.DECISION_JPDL_PROCESS_FILE_NAME);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(
                Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS),
                WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void testPath1() {
        val startVars = new HashMap<String, Object>();
        startVars.put("def_variable", "false");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_2", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getHrOperatorUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testPath2() {
        val startVars = new HashMap<String, Object>();
        startVars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        val state1Vars = new HashMap<String, Object>();
        state1Vars.put("monitoring_variable", "end");
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testPath3() {
        val startVars = new HashMap<String, Object>();
        startVars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        val state1Vars = new HashMap<String, Object>();
        state1Vars.put("monitoring_variable", "2");
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_2", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getHrOperatorUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testPath4() {
        val startVars = new HashMap<String, Object>();
        startVars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        val state1Vars = new HashMap<String, Object>();
        state1Vars.put("monitoring_variable", "3");
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_3", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getErpOperatorUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testPath5() {
        val startVars = new HashMap<String, Object>();
        startVars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        val state1Vars = new HashMap<String, Object>();
        state1Vars.put("monitoring_variable", "1");
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        state1Vars.clear();
        state1Vars.put("monitoring_variable", "1");
        for (int i = 0; i < 7; i++) {
            h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);
            tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
            assertEquals("tasks length differs from expected", 0, tasks.size());
            tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
            assertEquals("tasks length differs from expected", 0, tasks.size());
            tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
            assertEquals("tasks length differs from expected", 1, tasks.size());
            assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());
        }

        state1Vars.clear();
        state1Vars.put("monitoring_variable", "4");
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testPath6() {
        try {
            val startVars = new HashMap<String, Object>();
            startVars.put("def_variable", "Error_Var");
            executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

            List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
            assertEquals("tasks length differs from expected", 0, tasks.size());

            tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
            assertEquals("tasks length differs from expected", 1, tasks.size());

            tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
            assertEquals("tasks length differs from expected", 0, tasks.size());
        } catch (InternalApplicationException e) {
            // may be thrown in the future
        }
    }

    public void testPath7() {
        val startVars = new HashMap<String, Object>();
        startVars.put("def_variable", "true");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.DECISION_JPDL_PROCESS_NAME, startVars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        val state1Vars = new HashMap<String, Object>();
        state1Vars.put("monitoring_variable", "Error_Var2");
        try {
            h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), state1Vars);
            fail(" Integer in decision parsed value 'Error_Var2' ");
        } catch (Exception e) {
            // expected
        }
    }
}
