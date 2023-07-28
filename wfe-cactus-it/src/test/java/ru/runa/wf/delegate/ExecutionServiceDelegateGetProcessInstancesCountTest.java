package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetProcessInstancesCountTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        batchPresentation = h.getProcessInstanceBatchPresentation();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
        batchPresentation = null;
    }

    public void testGetProcessInstanceCountByAuthorizedUser() {
        int processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    public void testGetProcessInstanceCountByUnauthorizedUser() {
        int processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getProcessesCount(h.getUnauthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
    }

    public void testGetProcessInstanceCountByFakeUser() {
        try {
            executionService.getProcessesCount(h.getFakeUser(), batchPresentation);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetProcessInstanceCountByAuthorizedUserWithoutREADPermission() {
        int processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        List<WfProcess> processInstanceStubs = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);

        Collection<Permission> nullPermissions = Lists.newArrayList();
        int withoutPermCount = processInstanceStubs.size() / 2;
        for (int i = 0; i < withoutPermCount; i++) {
            h.setPermissionsToAuthorizedActorOnProcessInstance(nullPermissions, processInstanceStubs.get(i));
        }

        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - withoutPermCount, processesCount);
    }

    public void testGetProcessInstanceCountWithSorting() {
        int processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 20;
        startInstances(expectedCount);
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToSort(new int[] { 0, 1, 2, 3 }, new boolean[] { true, false, true, false });
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    public void testGetProcessInstanceCountWithGrouping() {
        int processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 20;
        startInstances(expectedCount);
        batchPresentation.setFieldsToGroup(new int[] { 0 });
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToGroup(new int[] { 0, 1 });
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToGroup(new int[] { 0, 1, 2, 3 });
        processesCount = executionService.getProcessesCount(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    private void startInstances(int instanceCount) {
        for (int i = 0; i < instanceCount; i++) {
            executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        }
    }

}
