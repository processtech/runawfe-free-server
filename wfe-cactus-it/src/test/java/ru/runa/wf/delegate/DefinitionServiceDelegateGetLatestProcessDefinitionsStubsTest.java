package ru.runa.wf.delegate;

import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetLatestProcessDefinitionsStubsTest extends ServletTestCase {
    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();
        batchPresentation = helper.getProcessDefinitionBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        definitionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetLatestProcessDefinitionsStubsByAuthorizedSubject() throws Exception {
        List<WfDefinition> processes = definitionService.getProcessDefinitions(helper.getAuthorizedPerformerUser(), batchPresentation, false);

        assertEquals("definitionDelegate.getLatestDefinitionStub() returned not expected list", 1, processes.size());
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", processes.get(0).getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestProcessDefinitionsStubsByUnauthorizedSubject() throws Exception {
        List<WfDefinition> processes;
        try {
            processes = definitionService.getProcessDefinitions(helper.getUnauthorizedPerformerUser(), batchPresentation, false);
            assertEquals("testGetLatestDefinitionStubByUnauthorizedSubject returns process definition for unauthorized performer", 0,
                    processes.size());
        } catch (AuthorizationException e) {
        }
    }

    public void testGetLatestProcessDefinitionsStubsByFakeSubject() throws Exception {
        try {
            definitionService.getProcessDefinitions(helper.getFakeUser(), batchPresentation, false);
            assertTrue("testGetLatestDefinitionStubByUnauthorizedSubject, no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetLatestProcessDefinitionsStubsByNullSubject() throws Exception {
        try {
            definitionService.getProcessDefinitions(null, batchPresentation, false);
            assertTrue("testGetLatestProcessDefinitionsStubsByNullSubject, no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }
}
