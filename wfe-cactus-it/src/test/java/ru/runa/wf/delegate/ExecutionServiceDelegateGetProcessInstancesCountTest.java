package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
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
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        batchPresentation = helper.getProcessInstanceBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetProcessInstanceCountByAuthorizedSubject() throws Exception {
        int processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    public void testGetProcessInstanceCountByUnauthorizedSubject() throws Exception {
        int processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getProcessesCount(helper.getUnauthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
    }

    public void testGetProcessInstanceCountByFakeSubject() throws Exception {
        try {
            executionService.getProcessesCount(helper.getFakeUser(), batchPresentation);
            fail("testGetAllProcessInstanceStubsByFakeSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessInstanceCountByAuthorizedSubjectWithoutREADPermission() throws Exception {
        int processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        List<WfProcess> processInstanceStubs = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);

        Collection<Permission> nullPermissions = Lists.newArrayList();
        int withoutPermCount = processInstanceStubs.size() / 2;
        for (int i = 0; i < withoutPermCount; i++) {
            helper.setPermissionsToAuthorizedPerformerOnProcessInstance(nullPermissions, processInstanceStubs.get(i));
        }

        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - withoutPermCount, processesCount);
    }

    public void testGetProcessInstanceCountWithSorting() throws Exception {
        int processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 20;
        startInstances(expectedCount);
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToSort(new int[] { 0, 1, 2, 3 }, new boolean[] { true, false, true, false });
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    public void testGetProcessInstanceCountWithGrouping() throws Exception {
        int processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 20;
        startInstances(expectedCount);
        batchPresentation.setFieldsToGroup(new int[] { 0 });
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToGroup(new int[] { 0, 1 });
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToGroup(new int[] { 0, 1, 2, 3 });
        processesCount = executionService.getProcessesCount(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    private void startInstances(int instanceCount) throws InternalApplicationException {
        for (int i = 0; i < instanceCount; i++) {
            executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        }
    }

}
