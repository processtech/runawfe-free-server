package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetStartFormTest extends ServletTestCase {
    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private Long definitionId;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();

        definitionId = definitionService.getLatestProcessDefinition(helper.getAdminUser(), WfServiceTestHelper.VALID_PROCESS_NAME).getVersionId();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetStartFormTestByAuthorizedSubject() throws Exception {
        Interaction startForm = definitionService.getStartInteraction(helper.getAuthorizedPerformerUser(), definitionId);

        // / TO DO : xml read from forms.xml & processdefinition.xml
        // TODO assertEquals("start form name differ from original",
        // "request a payraise", startForm.getStateName());
        if (false) {
            assertEquals("start form name differ from original", "html", startForm.getType());
            Map<String, VariableDefinition> vars = startForm.getVariables();
            List<String> actual = new ArrayList<String>();
            for (VariableDefinition var : vars.values()) {
                actual.add(var.getName());
            }

            List<String> expected = Lists.newArrayList("reason", "amount.asked", "time", "file", "actor");
            ArrayAssert.assertWeakEqualArrays("Variables from start from differ from declaration", expected, actual);
        }
    }

    public void testGetStartFormTestByUnauthorizedSubject() throws Exception {
        definitionService.getStartInteraction(helper.getUnauthorizedPerformerUser(), definitionId);
    }

    public void testGetStartFormTestByFakeSubject() throws Exception {
        try {
            definitionService.getStartInteraction(helper.getFakeUser(), definitionId);
            fail("testGetStartFormTestByFakeSubject , no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetStartFormTestByAuthorizedSubjectWithInvalidDefinitionId() throws Exception {
        try {
            definitionService.getStartInteraction(helper.getAuthorizedPerformerUser(), -1l);
            fail("testGetStartFormTestByAuthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (DefinitionDoesNotExistException e) {
        }
    }
}
