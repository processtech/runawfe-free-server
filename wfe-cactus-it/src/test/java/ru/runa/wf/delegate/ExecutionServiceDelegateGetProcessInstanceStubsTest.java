package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 */
public class ExecutionServiceDelegateGetProcessInstanceStubsTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        batchPresentation = h.getProcessInstanceBatchPresentation();

        h.deployValidProcessDefinition();
        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        val pp = Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(pp, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(pp, WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
        batchPresentation = null;
    }

    public void testGetProcessInstanceStubsByVariableFilterByAuthorizedUser() {
        String name = "reason";
        String value = "intention";
        Map<String, Object> variablesMap = WfServiceTestHelper.createVariablesMap(name, value);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        variablesMap.put(name, "anothervalue");
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        int index = batchPresentation.getType().getFieldIndex(CurrentProcessClassPresentation.PROCESS_VARIABLE);
        batchPresentation.addDynamicField(index, name);
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria(value));
        List<WfProcess> processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals(2, processes.size());
    }

    public void testGetProcessInstanceStubsByVariableFilterWithWrongMatcherByAuthorizedUser() {
        String name = "reason";
        String value = "intention";
        Map<String, Object> variablesMap = WfServiceTestHelper.createVariablesMap(name, value);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        int index = batchPresentation.getType().getFieldIndex(CurrentProcessClassPresentation.PROCESS_VARIABLE);
        batchPresentation.addDynamicField(index, name);
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria("bad matcher"));
        List<WfProcess> processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals(0, processes.size());
    }

    public void testGetProcessInstanceStubsByAuthorizedUser() {
        List<WfProcess> processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 1, processes.size());
    }

    public void testGetProcessInstanceStubsByUnauthorizedUser() {
        List<WfProcess> processes = executionService.getProcesses(h.getUnauthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processes = executionService.getProcesses(h.getUnauthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
    }

    public void testGetProcessInstanceStubsByFakeUser() {
        try {
            executionService.getProcesses(h.getFakeUser(), batchPresentation);
            fail();
        } catch (AuthenticationException e) {
            // Expeced.
        }
    }

    public void testGetProcessInstanceStubsByAuthorizedUserWithoutREADPermission() {
        List<WfProcess> processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processes = executionService.getProcesses(h.getAdminUser(), batchPresentation);
        assertEquals("Incorrect processes array", 1, processes.size());
        Collection<Permission> nullPermissions = Lists.newArrayList();
        h.setPermissionsToAuthorizedActorOnProcessInstance(nullPermissions, processes.get(0));
        processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
    }

    public void testGetProcessInstanceStubsPagingByAuthorizedUser() {
        List<WfProcess> processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());

        int rangeSize = 10;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);

        int expectedCount = 14;
        for (int i = 0; i < expectedCount; i++) {
            executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        }
        processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", rangeSize, processes.size());

        batchPresentation.setPageNumber(2);

        processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - rangeSize, processes.size());

        rangeSize = 50;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);

        processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processes.size());
    }

    public void testGetProcessInstanceStubsUnexistentPageByAuthorizedUser() {
        List<WfProcess> processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());

        int rangeSize = 10;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);
        batchPresentation.setFieldsToSort(new int[] { 3 }, new boolean[] { true });

        int expectedCount = 17;
        for (int i = 0; i < expectedCount; i++) {
            executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        }
        List<WfProcess> firstTenProcesses = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", rangeSize, firstTenProcesses.size());

        batchPresentation.setPageNumber(2);
        processes = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - 10, processes.size());

        // the wrong page is replaced by last page in case it contains 0 objects
        batchPresentation.setPageNumber(3);

        List<WfProcess> wrongPageProcesses = executionService.getProcesses(h.getAuthorizedUser(), batchPresentation);
        // due to
        // ru.runa.wfe.presentation.BatchPresentation.setFilteredFieldsMap(Map<Integer,
        // FilterCriteria>) in hibernate.update
        ArrayAssert.assertEqualArrays("Incorrect returned", firstTenProcesses, wrongPageProcesses);
    }
}
