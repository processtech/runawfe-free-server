package ru.runa.wf.delegate;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetProcessInstanceStubTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Long processId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition();

        // processId =
        executionService.startProcess(h.getAdminUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processId = executionService.getProcesses(h.getAuthorizedUser(), h.getProcessInstanceBatchPresentation()).get(0).getId();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
    }

    public void testGetProcessInstanceStubByAuthorizedUser() {
        WfProcess processInstance = executionService.getProcess(h.getAuthorizedUser(), processId);
        assertEquals("id of running process differs from requested", processId, processInstance.getId());
        assertEquals("name of running process differs from definition", WfServiceTestHelper.VALID_PROCESS_NAME, processInstance.getName());
    }

    public void testGetProcessInstanceStubByUnauthorizedUser() {
        try {
            executionService.getProcess(h.getUnauthorizedUser(), processId);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetProcessInstanceStubByFakeUser() {
        try {
            executionService.getProcess(h.getFakeUser(), processId);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetProcessInstanceStubByAuthorizedUserWithInvalidProcessId() {
        try {
            executionService.getProcess(h.getAuthorizedUser(), -1L);
            fail();
        } catch (ProcessDoesNotExistException e) {
            // Expected.
        }
    }
}
