package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateDeployProcessDefinitionTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_DEFINITION), SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        definitionService = null;
    }

    public void testDeployInvalidProcessByAuthorizedUser() {
        try {
            definitionService
                    .deployProcessDefinition(h.getAuthorizedUser(), h.getInValidProcessDefinition(), Lists.newArrayList("testProcess"), null);
            fail();
        } catch (DefinitionArchiveFormatException e) {
            // Expected.
        }
    }
}
