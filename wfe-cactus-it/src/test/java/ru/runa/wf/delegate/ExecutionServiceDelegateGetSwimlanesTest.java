package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateGetSwimlanesTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;
    private Long instanceId;


    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        batchPresentation = h.getProcessInstanceBatchPresentation();

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(
                Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS), WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.READ), h.getAuthorizedActor());

        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);
        instanceId = executionService.getProcesses(h.getAdminUser(), batchPresentation).get(0).getId();

        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
        batchPresentation = null;
    }

    public void testGetSwimlanesByUnauthorizedUser() {
        try {
            executionService.getProcessSwimlanes(h.getUnauthorizedUser(), instanceId);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetSwimlanesByFakeUser() {
        try {
            executionService.getProcessSwimlanes(h.getFakeUser(), instanceId);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetSwimlanesByAuthorizedUserWithInvalidProcessId() {
        try {
            executionService.getProcessSwimlanes(h.getAuthorizedUser(), -1L);
            fail();
        } catch (ProcessDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetSwimlanesByAuthorizedUser() {
        List<WfSwimlane> WfSwimlanes = executionService.getProcessSwimlanes(h.getAuthorizedUser(), instanceId);
        List<String> expectedNames = Lists.newArrayList("boss", "requester", "erp operator");
        List<String> actualNames = Lists.newArrayList();
        for (WfSwimlane WfSwimlane : WfSwimlanes) {
            actualNames.add(WfSwimlane.getDefinition().getName());
            if (WfSwimlane.getDefinition().getName().equals("requester")) {
                assertNotNull("swimlane is not assigned", WfSwimlane.getExecutor());
                assertEquals("Actor differs from Assigned", h.getAuthorizedActor(), WfSwimlane.getExecutor());
            }
        }
        ArrayAssert.assertWeakEqualArrays("swimlane names are not equal", expectedNames, actualNames);
    }
}
