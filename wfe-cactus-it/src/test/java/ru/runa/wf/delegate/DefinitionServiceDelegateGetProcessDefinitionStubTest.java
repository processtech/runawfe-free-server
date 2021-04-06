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
    private WfServiceTestHelper h = null;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testGetProcessDefinitionStubByAuthorizedUser() {
        Collection<Permission> permissions = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        WfDefinition process = definitionService.getLatestProcessDefinition(h.getAuthorizedUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        long processId = process.getVersionId();
        WfDefinition actualProcess = definitionService.getProcessDefinition(h.getAuthorizedUser(), processId);
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getName(),
                actualProcess.getName());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getVersionId(),
                actualProcess.getVersionId());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getVersionId(),
                actualProcess.getVersionId());
    }

    public void testGetProcessDefinitionStubByAuthorizedUserWithoutREADPermission() {
        Collection<Permission> permissions = Lists.newArrayList();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            definitionService.getLatestProcessDefinition(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetProcessDefinitionStubByUnauthorizedUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetProcessDefinitionStubByFakeUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
