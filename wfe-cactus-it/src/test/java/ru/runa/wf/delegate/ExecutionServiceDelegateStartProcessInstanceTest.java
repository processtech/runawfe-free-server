package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateStartProcessInstanceTest extends ServletTestCase {
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

    public void testStartProcessInstanceByAuthorizedUser() {
        Long processInstanceId = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        assertNotNull(processInstanceId);
        List<WfProcess> processInstances = executionService.getProcesses(h.getAuthorizedUser(),
                h.getProcessInstanceBatchPresentation());
        assertEquals("Process not started", 1, processInstances.size());
        assertEquals(processInstanceId, processInstances.get(0).getId());
    }

    public void testStartProcessInstanceByUnauthorizedUser() {
        try {
            executionService.startProcess(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testStartProcessInstanceByFakeUser() {
        try {
            executionService.startProcess(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("expected AuthenticationException");
        } catch (InvalidDataAccessApiUsageException e) {
            fail("expected AuthenticationException");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testStartProcessInstanceByAuthorizedUserWithoutSTARTPermission() {
        Collection<Permission> noPermissions = Lists.newArrayList();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(noPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testStartProcessInstanceByAuthorizedUserWithoutREADPermission() {
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS), WfServiceTestHelper.VALID_PROCESS_NAME);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        List<WfProcess> processInstances = executionService.getProcesses(h.getAdminUser(), h.getProcessInstanceBatchPresentation());
        assertEquals(1, processInstances.size());
    }

    public void testStartProcessInstanceByAuthorizedUserWithInvalidProcessName() {
        try {
            executionService.startProcess(h.getAuthorizedUser(), "0_INVALID_PROCESS_NAME", null);
            fail("executionDelegate.startProcessInstance(subj, invalid name), no DefinitionDoesNotExistException");
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }
}
