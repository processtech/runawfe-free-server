package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
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
 * 
 */
public class DefinitionServiceDelegateGetLatestDefinitionStubTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.READ), WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testGetLatestDefinitionStubByAuthorizedUser() {
        WfDefinition process = definitionService.getLatestProcessDefinition(h.getAuthorizedUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", process.getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestDefinitionStubByUnauthorizedUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetLatestDefinitionStubByFakeUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetLatestDefinitionStubByAuthorizedUserWithInvalidProcessName() {
        try {
            definitionService.getLatestProcessDefinition(h.getAuthorizedUser(), "0_Invalid_Process_Name");
            fail();
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }
}
