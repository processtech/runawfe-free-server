package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetProcessDefinitionStubTest extends ServletTestCase {
    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetProcessDefinitionStubByAuthorizedSubject() throws Exception {
        Collection<Permission> permissions = Lists.newArrayList(Permission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        WfDefinition process = definitionService.getLatestProcessDefinition(helper.getAuthorizedPerformerUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        long processId = process.getVersionId();
        WfDefinition actualProcess = definitionService.getProcessDefinition(helper.getAuthorizedPerformerUser(), processId);
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getName(),
                actualProcess.getName());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getVersionId(),
                actualProcess.getVersionId());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getVersionId(),
                actualProcess.getVersionId());
    }

    public void testGetProcessDefinitionStubByAuthorizedSubjectWithoutREADPermission() throws Exception {
        Collection<Permission> permissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            definitionService.getLatestProcessDefinition(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByAuthorizedSubjectWithourREADPermission(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessDefinitionStubByUnauthorizedSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinition(helper.getUnauthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessDefinitionStubByFakeSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinition(helper.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }
}
