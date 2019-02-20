package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ExecutorPermission;

import com.google.common.collect.Lists;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateGetSwimlanesTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Long instanceId;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ,
                DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        permissions = Lists.newArrayList(ExecutorPermission.READ);
        helper.setPermissionsToAuthorizedPerformer(permissions, helper.getAuthorizedPerformerActor());

        // instanceId =
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);
        batchPresentation = helper.getProcessInstanceBatchPresentation();
        instanceId = executionService.getProcesses(helper.getAdminUser(), batchPresentation).get(0).getId();

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetSwimlanesByUnauthorizedSubject() throws Exception {
        try {
            executionService.getProcessSwimlanes(helper.getUnauthorizedPerformerUser(), instanceId);
            fail("testGetSwimlanesByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetSwimlanesByFakeSubject() throws Exception {
        try {
            executionService.getProcessSwimlanes(helper.getFakeUser(), instanceId);
            fail("testGetSwimlanesByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetSwimlanesByNullSubject() throws Exception {
        try {
            executionService.getProcessSwimlanes(null, instanceId);
            fail("testGetSwimlanesByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetSwimlanesByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getProcessSwimlanes(helper.getAuthorizedPerformerUser(), -1l);
            fail("testGetSwimlanesByAuthorizedSubjectWithInvalidProcessId(), no ProcessInstanceDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
        }
    }

    public void testGetSwimlanesByAuthorizedSubject() throws Exception {
        List<WfSwimlane> WfSwimlanes = executionService.getProcessSwimlanes(helper.getAuthorizedPerformerUser(), instanceId);
        List<String> expectedNames = Lists.newArrayList("boss", "requester", "erp operator");
        List<String> actualNames = Lists.newArrayList();
        for (WfSwimlane WfSwimlane : WfSwimlanes) {
            actualNames.add(WfSwimlane.getDefinition().getName());
            if (WfSwimlane.getDefinition().getName().equals("requester")) {
                assertTrue("swimlane is not assigned", WfSwimlane.getExecutor() != null);
                assertEquals("Actor differs from Assigned", helper.getAuthorizedPerformerActor(), WfSwimlane.getExecutor());
            }
        }
        ArrayAssert.assertWeakEqualArrays("swimlane names are not equal", expectedNames, actualNames);
    }
}
