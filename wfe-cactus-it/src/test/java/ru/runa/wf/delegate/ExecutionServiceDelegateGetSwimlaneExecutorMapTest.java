package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.HashMap;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetSwimlaneExecutorMapTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Long instanceId;
    private HashMap<String, Object> legalVariables;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
        instanceId = executionService.getProcesses(h.getAdminUser(), h.getProcessInstanceBatchPresentation()).get(0).getId();

        legalVariables = new HashMap<>();
        legalVariables.put("amount.asked", 200d);
        legalVariables.put("amount.granted", 150d);
        legalVariables.put("approved", "true");
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void testGetSwimlaneExecutorMapByUnauthorizedUser() {
        try {
            h.getTaskService().getProcessTasks(h.getUnauthorizedUser(), instanceId, true);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetSwimlaneExecutorMapByFakeUser() {
        try {
            h.getTaskService().getProcessTasks(h.getFakeUser(), instanceId, true);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedUserWithInvalidProcessId() {
        try {
            h.getTaskService().getProcessTasks(h.getAuthorizedUser(), -1L, true);
            fail();
        } catch (ProcessDoesNotExistException e) {
            // Expected.
        }
    }

}
