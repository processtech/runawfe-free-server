package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

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

        Collection<Permission> deployPermissions = Lists.newArrayList(Permission.CREATE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitions(deployPermissions);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testDeployInvalidProcessByAuthorizedPerformer() throws Exception {
        try {
            definitionService.deployProcessDefinition(helper.getAuthorizedPerformerUser(), helper.getInValidProcessDefinition(),
                    Lists.newArrayList("testProcess"), null);
            assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionArchiveFormatException", false);
        } catch (DefinitionArchiveFormatException e) {
            // That's what we expect
        }
    }
}
