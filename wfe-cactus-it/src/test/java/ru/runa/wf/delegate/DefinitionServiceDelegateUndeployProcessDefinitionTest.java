package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
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
 */
public class DefinitionServiceDelegateUndeployProcessDefinitionTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.DELETE), WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        definitionService = null;
    }

    public void testUndeployProcessByAuthorizedUser() {
        definitionService.undeployProcessDefinition(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        List<WfDefinition> deployedProcesses = definitionService.getProcessDefinitions(h.getAuthorizedUser(),
                h.getProcessDefinitionBatchPresentation(), false);

        if (deployedProcesses.size() != 0) {
            fail("testUndeployProcessByAuthorizedUser() wrongNumberOfProcessDefinitions after undeployment");
        }

        try {
            definitionService.undeployProcessDefinition(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testUndeployProcessByAuthorizedUser() allows undeploy process definition after undeployment");
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }

    public void testUndeployProcessByAuthorizedUserWithoutUNDEPLOYPermission() {
        try {
            Collection<Permission> undeployPermissions = Lists.newArrayList();
            h.setPermissionsToAuthorizedActorOnDefinitionByName(undeployPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

            try {
                definitionService.undeployProcessDefinition(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
                fail("testUndeployProcessByAuthorizedUserWithoutUNDEPLOYPermission(), no AuthorizationException");
            } catch (AuthorizationException e1) {
                // Expected.
            }
        } finally {
            h.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessByUnauthorizedUser() {
        try {
            definitionService.undeployProcessDefinition(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testUndeployProcessByUnauthorizedUser(), no AuthorizationException");
        } catch (AuthorizationException e1) {
            // Expected.
        } finally {
            h.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessByFakeUser() {
        try {
            definitionService.undeployProcessDefinition(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("no AuthenticationException");
        } catch (AuthenticationException e1) {
            // Expected.
        } finally {
            h.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessWithUnexistentProcessName() {
        try {
            definitionService.undeployProcessDefinition(h.getUnauthorizedUser(), "Unexistent_Process_definition_Name_000", null);
            fail("testUndeployProcessWithNullProcessName allows undeploy process definition with unexistent name");
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        } finally {
            h.undeployValidProcessDefinition();
        }
    }
}
