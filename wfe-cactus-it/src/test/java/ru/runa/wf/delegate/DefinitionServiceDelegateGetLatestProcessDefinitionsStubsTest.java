package ru.runa.wf.delegate;

import java.util.List;

import lombok.val;
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
    private WfServiceTestHelper h;
    private DefinitionService definitionService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        batchPresentation = h.getProcessDefinitionBatchPresentation();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
        batchPresentation = null;
    }

    public void testGetLatestProcessDefinitionsStubsByAuthorizedUser() {
        List<WfDefinition> processes = definitionService.getProcessDefinitions(h.getAuthorizedUser(), batchPresentation, false);

        assertEquals("definitionDelegate.getLatestDefinitionStub() returned not expected list", 1, processes.size());
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", processes.get(0).getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestProcessDefinitionsStubsByUnauthorizedUser() {
        try {
            val processes = definitionService.getProcessDefinitions(h.getUnauthorizedUser(), batchPresentation, false);
            assertEquals(0, processes.size());
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetLatestProcessDefinitionsStubsByFakeUser() {
        try {
            definitionService.getProcessDefinitions(h.getFakeUser(), batchPresentation, false);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
