package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

/**
 * @author Pashkov Alexander
 */
public class DefinitionServiceDelegateGetDeclaredVariablesNamesTest extends ServletTestCase {
    private static final String DEFINITION_WITH_VARIABLES_XML = "processWithVariablesXml";

    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    private long definitionWithVariablesXmlId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_DEFINITION), SecuredSingleton.SYSTEM);

        definitionService.deployProcessDefinition(h.getAuthorizedUser(),
                WfServiceTestHelper.readBytesFromFile(DEFINITION_WITH_VARIABLES_XML + ".par"), Lists.newArrayList("testProcess"), null);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.READ), DEFINITION_WITH_VARIABLES_XML);

        definitionWithVariablesXmlId = definitionService
                .getLatestProcessDefinition(h.getAuthorizedUser(), DEFINITION_WITH_VARIABLES_XML).getId();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(DEFINITION_WITH_VARIABLES_XML);
        h.releaseResources();
        definitionService = null;
    }

    public void testGetDeclaredVariablesNamesByAuthorizedUser() {
        testImpl(h.getAuthorizedUser(), definitionWithVariablesXmlId, Lists.newArrayList("var1", "Var2", "var3"), null);
    }

    public void testGetDeclaredVariablesNamesOnInvalidDefinitionId() {
        testImpl(h.getAuthorizedUser(), -1, null, DefinitionDoesNotExistException.class);
    }

    private void testImpl(User user, long definitionId, List<String> expected, Class<? extends Exception> exception) {
        List<VariableDefinition> actual;
        try {
            actual = definitionService.getVariableDefinitions(user, definitionId);
        } catch (Exception e) {
            assertEquals(exception, e.getClass());
            return;
        }
        if (exception != null) {
            fail("exception expected");
        }

        List<String> actualNames = Lists.newArrayList();
        for (VariableDefinition var : actual) {
            actualNames.add(var.getName());
        }
        ArrayAssert.assertWeakEqualArrays("", expected, actualNames);
    }
}
