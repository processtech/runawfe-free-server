package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 */
public class ExecutionServiceDelegateGetProcessInstanceStubsTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);
        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        Collection<Permission> startPermissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        batchPresentation = helper.getProcessInstanceBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetProcessInstanceStubsByVariableFilterByAuthorizedSubject() throws Exception {
        String name = "reason";
        String value = "intention";
        Map<String, Object> variablesMap = WfServiceTestHelper.createVariablesMap(name, value);
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        variablesMap.put(name, "anothervalue");
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        int index = batchPresentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.PROCESS_VARIABLE);
        batchPresentation.addDynamicField(index, name);
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria(value));
        List<WfProcess> processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals(2, processes.size());
    }

    public void testGetProcessInstanceStubsByVariableFilterWithWrongMatcherByAuthorizedSubject() throws Exception {
        String name = "reason";
        String value = "intention";
        Map<String, Object> variablesMap = WfServiceTestHelper.createVariablesMap(name, value);
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        int index = batchPresentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.PROCESS_VARIABLE);
        batchPresentation.addDynamicField(index, name);
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria("bad matcher"));
        List<WfProcess> processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals(0, processes.size());
    }

    public void testGetProcessInstanceStubsByAuthorizedSubject() throws Exception {
        List<WfProcess> processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 1, processes.size());
    }

    public void testGetProcessInstanceStubsByUnauthorizedSubject() throws Exception {
        List<WfProcess> processes = executionService.getProcesses(helper.getUnauthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processes = executionService.getProcesses(helper.getUnauthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
    }

    public void testGetProcessInstanceStubsByFakeSubject() throws Exception {
        try {
            executionService.getProcesses(helper.getFakeUser(), batchPresentation);
            fail("testGetAllProcessInstanceStubsByFakeSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessInstanceStubsByNullSubject() throws Exception {
        try {
            executionService.getProcesses(null, batchPresentation);
            fail("testGetAllProcessInstanceStubsByNullSubject, no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetProcessInstanceStubsByAuthorizedSubjectWithoutREADPermission() throws Exception {
        List<WfProcess> processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        processes = executionService.getProcesses(helper.getAdminUser(), batchPresentation);
        assertEquals("Incorrect processes array", 1, processes.size());
        Collection<Permission> nullPermissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(nullPermissions, processes.get(0));
        processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
    }

    public void testGetProcessInstanceStubsPagingByAuthorizedSubject() throws Exception {
        List<WfProcess> processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());

        int rangeSize = 10;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);

        int expectedCount = 14;
        for (int i = 0; i < expectedCount; i++) {
            executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        }
        processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", rangeSize, processes.size());

        batchPresentation.setPageNumber(2);

        processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - rangeSize, processes.size());

        rangeSize = 50;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);

        processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processes.size());
    }

    public void testGetProcessInstanceStubsUnexistentPageByAuthorizedSubject() throws Exception {
        List<WfProcess> processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());

        int rangeSize = 10;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);
        batchPresentation.setFieldsToSort(new int[] { 3 }, new boolean[] { true });

        int expectedCount = 17;
        for (int i = 0; i < expectedCount; i++) {
            executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        }
        List<WfProcess> firstTenProcesses = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", rangeSize, firstTenProcesses.size());

        batchPresentation.setPageNumber(2);
        processes = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - 10, processes.size());

        // the wrong page is replaced by last page in case it contains 0 objects
        batchPresentation.setPageNumber(3);

        List<WfProcess> wrongPageProcesses = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        // due to
        // ru.runa.wfe.presentation.BatchPresentation.setFilteredFieldsMap(Map<Integer,
        // FilterCriteria>) in hibernate.update
        ArrayAssert.assertEqualArrays("Incorrect returned", firstTenProcesses, wrongPageProcesses);
    }

}
