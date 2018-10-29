package ru.runa.wf.delegate;

import java.util.Collection;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Powered by Dofs
 */
public class DefinitionServiceDelegateGetFileTest extends ServletTestCase {

    private DefinitionService definitionService = null;

    private WfServiceTestHelper helper = null;

    private long definitionId;

    private final String VALID_FILE_NAME = "description.txt";

    private final String INVALID_FILE_NAME = "processdefinitioninvalid.xml";

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        definitionId = definitionService.getLatestProcessDefinition(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME)
                .getId();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetFileTestByAuthorizedSubject() throws Exception {
        byte[] fileBytes = definitionService.getProcessDefinitionFile(helper.getAuthorizedPerformerUser(), definitionId, VALID_FILE_NAME);
        assertNotNull("file bytes is null", fileBytes);
    }

    /*
     * We allowing that now public void testGetFileTestByUnauthorizedSubject()
     * throws Exception { try {
     * definitionDelegate.getFile(helper.getUnauthorizedPerformerUser(),
     * definitionId, VALID_FILE_NAME);
     * assertTrue("testGetFileTestByUnauthorizedSubject , no AuthorizationException"
     * , false); } catch (AuthorizationException e) { } }
     */

    public void testGetFileTestByNullUser() throws Exception {
        try {
            definitionService.getProcessDefinitionFile(null, definitionId, VALID_FILE_NAME);
            assertTrue("testGetFormTestByNullSubject , no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetFileTestByFakeSubject() throws Exception {
        try {
            definitionService.getProcessDefinitionFile(helper.getFakeUser(), definitionId, VALID_FILE_NAME);
            assertTrue("testGetFileTestByFakeSubject , no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetFileTestByAuthorizedSubjectWithInvalidDefinitionId() throws Exception {
        try {
            definitionService.getProcessDefinitionFile(helper.getAuthorizedPerformerUser(), -1L, VALID_FILE_NAME);
            fail("testGetFileTestByAuthorizedSubjectWithInvalidDefinitionId, no DefinitionDoesNotExistException");
        } catch (DefinitionDoesNotExistException e) {
            // expected
        }
    }

    public void testGetFileTestByAuthorizedSubjectWithInvalidFileName() throws Exception {
        try {
            definitionService.getProcessDefinitionFile(helper.getAuthorizedPerformerUser(), definitionId, INVALID_FILE_NAME);
            // TODO
            // fail("testGetFileTestByAuthorizedSubjectWithInvalidFileName, no ProcessDefinitionFileNotFoundException");
        } catch (DefinitionFileDoesNotExistException e) {
            // expected
            fail("TODO trap");
        }
    }
}
