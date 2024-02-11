package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 */
public class DefinitionServiceDelegateGetFormTest extends ServletTestCase {
    private final static String STATE_1_TYPE = "html";
    private final static String STATE_2_TYPE = "swt";
    private static final String VARIABLE_DEFAULT_FORMAT = "ru.runa.bpm.web.formgen.format.DefaultFormat";
    private static final String VARIABLE_DOUBLE_FORMAT = "ru.runa.bpm.web.formgen.format.DoubleFormat";

    private WfServiceTestHelper h = null;
    private DefinitionService definitionService;
    private WfTask task;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        definitionService.deployProcessDefinition(h.getAdminUser(),
                WfServiceTestHelper.readBytesFromFile(WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME), Lists.newArrayList("testProcess"), null);

        val pp = Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(pp, WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);

        Delegates.getExecutionService().startProcess(h.getAuthorizedUser(), WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME, null);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        definitionService = null;
    }

    private void initTaskData() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertTrue(tasks.size() > 0);
        task = tasks.get(0);
    }

    public void testGetFormTestByAuthorizedUser() {
        initTaskData();
        Interaction interaction = definitionService.getTaskNodeInteraction(h.getAuthorizedUser(), task.getDefinitionId(), task.getNodeId());
        // TODO assertEquals("form name differ from original", STATE_1_NAME,
        // interaction.getStateName());
        // TODO assertEquals("form name differ from original", STATE_1_TYPE,
        // interaction.getType());

    }

    public void testGetFormTestByUnauthorizedUser() {
        initTaskData();
        definitionService.getTaskNodeInteraction(h.getUnauthorizedUser(), task.getDefinitionId(), task.getNodeId());
    }

    public void testGetFormTestByFakeUser() {
        initTaskData();
        try {
            task = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation()).get(0);
            definitionService.getTaskNodeInteraction(h.getFakeUser(), task.getDefinitionId(), task.getNodeId());
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetFormTestByAuthorizedUserWithInvalidDefinitionId() {
        initTaskData();
        try {
            definitionService.getTaskNodeInteraction(h.getAuthorizedUser(), -1L, "");
            fail();
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }

    public void testCheckForm() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
        assertTrue(tasks.size() > 0);

        Interaction interaction = definitionService.getTaskNodeInteraction(h.getAuthorizedUser(), tasks.get(0).getDefinitionId(),
                tasks.get(0).getNodeId());

        // TODO assertEquals("state name differs from expected", STATE_1_NAME,
        // interaction.getStateName());
        if (false) {
            assertEquals("state type differs from expected", STATE_1_TYPE, interaction.getType());

            Map<String, VariableDefinition> variableDefinitions = interaction.getVariables();
            assertEquals("state variables count differs from expected", variableDefinitions.size(), 5);
            VariableDefinition var = variableDefinitions.get("requester");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertFalse("optional variable flag was set to false",
            // var.isOptional());

            var = variableDefinitions.get("reason");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertTrue("optional variable flag was set to true",
            // var.isOptional());

            var = variableDefinitions.get("amount.asked");
            assertEquals("variable format differs from expected", VARIABLE_DOUBLE_FORMAT, var.getFormatClassName());
            // assertFalse("optional variable flag was set to false",
            // var.isOptional());

            var = variableDefinitions.get("amount.granted");
            assertEquals("variable format differs from expected", VARIABLE_DOUBLE_FORMAT, var.getFormatClassName());
            // assertFalse("optional variable flag was set to false",
            // var.isOptional());

            var = variableDefinitions.get("approved");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertTrue("optional variable flag was set to true",
            // var.isOptional());

            h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

            tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
            interaction = definitionService.getTaskNodeInteraction(h.getAuthorizedUser(), tasks.get(0).getDefinitionId(),
                    tasks.get(0).getNodeId());

            // TODO assertEquals("state name differs from expected",
            // STATE_2_NAME, interaction.getStateName());
            fail("getStateName");
            assertEquals("state type differs from expected", STATE_2_TYPE, interaction.getType());

            variableDefinitions = interaction.getVariables();
            assertEquals("state variables count differs from expected", variableDefinitions.size(), 1);

            var = variableDefinitions.get("approved");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertTrue("optional variable flag was set to true",
            // var.isOptional());
        }
    }
}
