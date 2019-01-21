package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.collect.Lists;

/**
 * @author Pashkov Alexander
 */
public class DefinitionServiceDelegateGetDeclaredVariablesNamesTest extends ServletTestCase {

    private static final String DEFINITION_WITH_VARIABLES_XML = "processWithVariablesXml";

    private DefinitionService definitionService = null;

    private WfServiceTestHelper helper = null;

    private long definitionWithVariablesXmlId;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        Collection<Permission> deployPermissions = Lists.newArrayList(Permission.CREATE);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(deployPermissions);
        definitionService.deployProcessDefinition(helper.getAuthorizedPerformerUser(),
                WfServiceTestHelper.readBytesFromFile(DEFINITION_WITH_VARIABLES_XML + ".par"), Lists.newArrayList("testProcess"));

        Collection<Permission> permissions = Lists.newArrayList(Permission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, DEFINITION_WITH_VARIABLES_XML);

        definitionWithVariablesXmlId = definitionService
                .getLatestProcessDefinition(helper.getAuthorizedPerformerUser(), DEFINITION_WITH_VARIABLES_XML).getId();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(DEFINITION_WITH_VARIABLES_XML);
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetDeclaredVariablesNamesByAuthorizedSubject() {
        testImpl(helper.getAuthorizedPerformerUser(), definitionWithVariablesXmlId, Lists.newArrayList("var1", "Var2", "var3"), null);
    }

    public void testGetDeclaredVariablesNamesByUnauthorizedSubject() {
        // testImpl(helper.getUnauthorizedPerformerUser(),
        // definitionWithVariablesXmlId, null, AuthorizationException.class);
        // testImpl(helper.getUnauthorizedPerformerUser(),
        // definitionWithoutVariablesXmlId, null, AuthorizationException.class);
    }

    public void testGetDeclaredVariablesNamesByNullSubject() {
        // testImpl(null, definitionWithVariablesXmlId, null,
        // IllegalArgumentException.class);
        // testImpl(null, definitionWithoutVariablesXmlId, null,
        // IllegalArgumentException.class);
    }

    public void testGetDeclaredVariablesNamesByFakeSubject() {
        // testImpl(helper.getFakeUser(), definitionWithVariablesXmlId, null,
        // AuthenticationException.class);
        // testImpl(helper.getFakeUser(), definitionWithoutVariablesXmlId,
        // null, AuthenticationException.class);
    }

    public void testGetDeclaredVariablesNamesOnInvalidDefinitionId() {
        testImpl(helper.getAuthorizedPerformerUser(), -1, null, DefinitionDoesNotExistException.class);
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
