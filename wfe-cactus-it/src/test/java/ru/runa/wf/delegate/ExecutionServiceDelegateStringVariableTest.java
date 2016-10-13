package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Lists;

public class ExecutionServiceDelegateStringVariableTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition();
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testLongVariables() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        {
            String varName = "variable";
            String varValue = "";
            for (int i = 0; i < 200; ++i) {
                varValue = varValue + "-";
            }
            variables.put(varName, varValue);
        }
        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, variables);
        {
            String varName = "variable";
            String varValue = "";
            for (int i = 0; i < 300; ++i) {
                varValue = varValue + "-";
            }
            variables.put(varName, varValue);
        }
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAdminUser(), BatchPresentationFactory.TASKS.createDefault());
        th.getTaskService().completeTask(th.getAdminUser(), tasks.get(0).getId(), variables, null);
    }
}
