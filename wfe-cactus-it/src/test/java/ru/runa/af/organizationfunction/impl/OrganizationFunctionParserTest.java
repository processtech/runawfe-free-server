package ru.runa.af.organizationfunction.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Lists;

/**
 * @author Gritsenko_S
 */
public class OrganizationFunctionParserTest extends ServletTestCase {
    private final static String PREFIX = OrganizationFunctionParserTest.class.getName();

    private WfServiceTestHelper th;

    private static final String PROCESS_PATH = "organizationProcess.par";
    private static final String PROCESS_NAME = "organizationProcess";

    private Map<String, Object> legalVariables;

    @Override
    protected void setUp() throws Exception {

        th = new WfServiceTestHelper(PREFIX);

        th.addExecutorToGroup(th.getHrOperator(), th.getBossGroup());

        th.deployValidProcessDefinition(PROCESS_PATH);

        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(
                Lists.newArrayList(DefinitionPermission.READ, DefinitionPermission.START_PROCESS, DefinitionPermission.UNDEPLOY_DEFINITION),
                PROCESS_NAME);

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("approved", "true");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(PROCESS_NAME);
        th.releaseResources();
        th = null;
        super.tearDown();
    }

    public void testOrganizationFunction() throws Exception {
        th.getExecutionService().startProcess(th.getAuthorizedPerformerUser(), PROCESS_NAME, null);

        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAdminUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());
    }
}
