package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateCancelProcessInstanceTest extends ServletTestCase {
    private WfServiceTestHelper h = null;
    private ExecutionService executionService;
    private WfProcess processInstance = null;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition();
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processInstance = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation).get(0);
        batchPresentation = h.getProcessInstanceBatchPresentation();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
        batchPresentation = null;
    }

    public void testCancelProcessInstanceByAuthorizedUser() {
        h.setPermissionsToAuthorizedActorOnProcessInstance(Lists.newArrayList(Permission.CANCEL), processInstance);
        executionService.cancelProcess(h.getAuthorizedUser(), processInstance.getId());

        List<WfProcess> processInstances = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Process instance does not exist", 1, processInstances.size());
        assertEquals("Process not cancelled", ExecutionStatus.ENDED, processInstances.get(0).getExecutionStatus());
    }

    public void testCancelProcessInstanceByAuthorizedUserWithoutCANCELPermission() {
        h.setPermissionsToAuthorizedActorOnProcessInstance(Lists.newArrayList(Permission.READ), processInstance);
        try {
            executionService.cancelProcess(h.getAuthorizedUser(), processInstance.getId());
            List<WfProcess> processInstances = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
            assertEquals("Process instance does not exist", 1, processInstances.size());
            assertEquals("Process was canceled without CANCEL permission", ExecutionStatus.ACTIVE, processInstances.get(0).getExecutionStatus());
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testCancelProcessInstanceByFakeUser() {
        try {
            executionService.cancelProcess(h.getFakeUser(), processInstance.getId());
            fail("executionDelegate.cancelProcessInstance(h.getFakeUser(), ..), no AuthenticationException");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testCancelProcessInstanceByUnauthorizedUser() {
        try {
            executionService.cancelProcess(h.getUnauthorizedUser(), processInstance.getId());
            List<WfProcess> processInstances = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
            assertEquals("Process was cancelled by unauthorized subject", ExecutionStatus.ACTIVE, processInstances.get(0).getExecutionStatus());
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
