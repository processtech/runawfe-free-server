package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
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

    final String VARIABLE_DEFAULT_FORMAT = "ru.runa.bpm.web.formgen.format.DefaultFormat";

    final String VARIABLE_DOUBLE_FORMAT = "ru.runa.bpm.web.formgen.format.DoubleFormat";

    private ExecutionService executionService;

    private DefinitionService definitionService;

    private WfServiceTestHelper th = null;

    private WfTask task;

    protected static final long FAKE_ID = -1;
    protected static final String FAKE_NAME = "FAKE NAME OF TASK";

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();
        executionService = Delegates.getExecutionService();

        definitionService.deployProcessDefinition(th.getAdminUser(),
                WfServiceTestHelper.readBytesFromFile(WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME), Lists.newArrayList("testProcess"), null);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);

        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME, null);
        super.setUp();
    }

    private void initTaskData() throws AuthorizationException, AuthenticationException {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        task = tasks.get(0);
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);
        th.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetFormTestByAuthorizedSubject() throws Exception {
        initTaskData();
        Interaction interaction = definitionService.getTaskNodeInteraction(th.getAuthorizedPerformerUser(), task.getDefinitionId(), task.getNodeId());
        // TODO assertEquals("form name differ from original", STATE_1_NAME,
        // interaction.getStateName());
        // TODO assertEquals("form name differ from original", STATE_1_TYPE,
        // interaction.getType());

    }

    public void testGetFormTestByUnauthorizedSubject() throws Exception {
        initTaskData();
        definitionService.getTaskNodeInteraction(th.getUnauthorizedPerformerUser(), task.getDefinitionId(), task.getNodeId());
    }

    public void testGetFormTestByFakeSubject() throws Exception {
        initTaskData();
        try {
            task = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation()).get(0);
            definitionService.getTaskNodeInteraction(th.getFakeUser(), task.getDefinitionId(), task.getNodeId());
            fail("testGetFormTestByFakeSubject , no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetFormTestByAuthorizedSubjectWithInvalidDefinitionId() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskNodeInteraction(th.getAuthorizedPerformerUser(), -1l, "");
            fail("testGetFormTestByAuthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (DefinitionDoesNotExistException e) {
        }
    }

    public void testCheckForm() throws Exception {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
        assertEquals(tasks.size() > 0, true);

        Interaction interaction = definitionService.getTaskNodeInteraction(th.getAuthorizedPerformerUser(), tasks.get(0).getDefinitionId(),
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

            th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>(), null);

            tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
            interaction = definitionService.getTaskNodeInteraction(th.getAuthorizedPerformerUser(), tasks.get(0).getDefinitionId(),
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
