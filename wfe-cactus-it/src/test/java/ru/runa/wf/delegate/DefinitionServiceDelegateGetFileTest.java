package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Powered by Dofs
 */
public class DefinitionServiceDelegateGetFileTest extends ServletTestCase {
    private static final String VALID_FILE_NAME = "description.txt";
    private static final String INVALID_FILE_NAME = "processdefinitioninvalid.xml";

    private WfServiceTestHelper h = null;
    private DefinitionService definitionService = null;

    private long definitionId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.READ), WfServiceTestHelper.VALID_PROCESS_NAME);
        definitionId = definitionService.getLatestProcessDefinition(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME).getId();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testGetFileTestByAuthorizedUser() {
        byte[] fileBytes = definitionService.getProcessDefinitionFile(h.getAuthorizedUser(), definitionId, VALID_FILE_NAME);
        assertNotNull("file bytes is null", fileBytes);
    }

    /*
     * We allowing that now public void testGetFileTestByUnauthorizedUser() { try {
     * definitionDelegate.getFile(h.getUnauthorizedUser(), definitionId, VALID_FILE_NAME);
     * fail(); } catch (AuthorizationException e) { } }
     */

    public void testGetFileTestByFakeUser() {
        try {
            definitionService.getProcessDefinitionFile(h.getFakeUser(), definitionId, VALID_FILE_NAME);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetFileTestByAuthorizedUserWithInvalidDefinitionId() {
        try {
            definitionService.getProcessDefinitionFile(h.getAuthorizedUser(), -1L, VALID_FILE_NAME);
            fail();
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetFileTestByAuthorizedUserWithInvalidFileName() {
        try {
            definitionService.getProcessDefinitionFile(h.getAuthorizedUser(), definitionId, INVALID_FILE_NAME);
            // TODO
            // fail();
        } catch (DefinitionFileDoesNotExistException e) {
            // Expected.
            fail("TODO trap");
        }
    }
}
