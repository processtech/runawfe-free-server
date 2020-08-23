package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

public class ExecutionServiceDelegateStringVariableTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
    }

    public void testLongVariables() {
        val variables = new HashMap<String, Object>();
        {
            val varName = "variable";
            val varValue = new StringBuilder();
            for (int i = 0; i < 200; ++i) {
                varValue.append("-");
            }
            variables.put(varName, varValue.toString());
        }
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, variables);
        {
            val varName = "variable";
            val varValue = new StringBuilder();
            for (int i = 0; i < 300; ++i) {
                varValue.append("-");
            }
            variables.put(varName, varValue.toString());
        }
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAdminUser(), BatchPresentationFactory.TASKS.createDefault());
        h.getTaskService().completeTask(h.getAdminUser(), tasks.get(0).getId(), variables);
    }
}
