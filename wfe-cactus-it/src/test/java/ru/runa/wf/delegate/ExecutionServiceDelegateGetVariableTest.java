package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetVariableTest extends ServletTestCase {
    private static final String variableName = "var1";
    private static final String variableValue = "var1Value";

    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Long processId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition();

        val permissions = Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        val variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, variablesMap);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
    }

    private void initTaskId() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAdminUser(), h.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertTrue(tasks.size() > 0);
        processId = tasks.get(0).getProcessId();
    }

    public void testGetVariableByAuthorizedUser() {
        initTaskId();
        WfVariable variable = executionService.getVariable(h.getAuthorizedUser(), processId, variableName);
        assertEquals("variable has incorrect value", variableValue, variable.getValue());
    }
}
