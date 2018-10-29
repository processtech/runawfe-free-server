package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

import java.util.Collection;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateDeployProcessDefinitionTest extends ServletTestCase {

    private DefinitionService definitionService = null;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        Collection<Permission> deployPermissions = Lists.newArrayList(WorkflowSystemPermission.DEPLOY_DEFINITION);
        helper.setPermissionsToAuthorizedPerformerOnSystem(deployPermissions);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    /*
     * public void testDeployProcessByAuthorizedPerformer() throws Exception { try { definitionDelegate.deployProcessDefinition(helper.getAuthorizedPerformerUser(),
     * helper.getValidProcessDefinition(), new String []{"testProcess"}); ProcessDefinitionDescriptor[] deployedProcesses =
     * definitionDelegate.getLatestProcessDefinitionStubs(helper.getAuthorizedPerformerUser(), helper.getProcessDefinitionBatchPresentation()); if (deployedProcesses.length !=
     * 1) assertTrue("testDeployProcessByAuthorizedPerformer wrongNumberOfProcessDefinitions", false); if
     * (!deployedProcesses[0].getName().equals(WfServiceTestHelper.VALID_PROCESS_NAME)) assertTrue("testDeployProcessByAuthorizedPerformer wrongNameOfDeployedProcessDefinitions",
     * false);
     * 
     * try { definitionDelegate.deployProcessDefinition(helper.getAuthorizedPerformerUser(), helper.getValidProcessDefinition(), new String []{"testProcess"});
     * assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionAlreadyExistsException", false); } catch (ProcessDefinitionAlreadyExistsException e) {
     * //That's what we expect } } finally { helper.undeployValidProcessDefinition(); } }
     * 
     * public void testDeployProcessByAuthorizedPerformerWithoutDEPLOYPermission() throws Exception { Permission[] nullPermissions = {};
     * helper.setPermissionsToAuthorizedPerformerOnSystem(nullPermissions);
     * 
     * try { definitionDelegate.deployProcessDefinition(helper.getAuthorizedPerformerUser(), helper.getValidProcessDefinition(), new String []{"testProcess"});
     * assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionAlreadyExistsException", false); } catch (AuthorizationException e) { //That's what we
     * expect } }
     * 
     * public void testDeployProcessByUnauthorizedPerformer() throws Exception { try { definitionDelegate.deployProcessDefinition(helper.getUnauthorizedPerformerUser(),
     * helper.getValidProcessDefinition(), new String []{"testProcess"}); assertTrue("definitionDelegate.deployProcessByUnauthorizedPerformer() no AuthorizationFailedException",
     * false); } catch (AuthorizationException e) { //That's what we expect } }
     * 
     * public void testDeployProcessWithNullSubject() throws Exception { try { definitionDelegate.deployProcessDefinition(null, helper.getValidProcessDefinition(), new String
     * []{"testProcess"}); assertTrue("DeployProcessWithNullSubject no Exception", false); } catch (InternalApplicationException e) { //That's what we expect while(e.getCause() !=
     * null && e.getCause() instanceof InternalApplicationException) e = (InternalApplicationException)e.getCause(); if(!(e.getCause() instanceof IllegalArgumentException)) throw
     * e; } }
     * 
     * public void testDeployProcessWithFakeSubject() throws Exception { try { Subject fakeSubject = helper.getFakeUser();
     * definitionDelegate.deployProcessDefinition(fakeSubject, helper.getValidProcessDefinition(), new String []{"testProcess"});
     * assertTrue("testDeployProcessWithoutPermission no Exception", false); } catch (AuthenticationException e) { //That's what we expect } }
     */

    public void testDeployInvalidProcessByAuthorizedPerformer() throws Exception {
        try {
            definitionService.deployProcessDefinition(
                    helper.getAuthorizedPerformerUser()
                    , helper.getInValidProcessDefinition()
                    , Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionArchiveFormatException", false);
        } catch (DefinitionArchiveFormatException e) {
            // That's what we expect
        }
    }

    /*
     * public void testDeployNullProcessByAuthorizedPerformer() throws Exception { try { definitionDelegate.deployProcessDefinition(helper.getAuthorizedPerformerUser(), null,
     * new String []{"testProcess"}); assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer(subject, null) no IllegalArgumentException", false); } catch
     * (InternalApplicationException e) { //That's what we expect while(e.getCause() != null && e.getCause() instanceof InternalApplicationException) e =
     * (InternalApplicationException)e.getCause(); if(!(e.getCause() instanceof IllegalArgumentException)) throw e; } }
     */
}
