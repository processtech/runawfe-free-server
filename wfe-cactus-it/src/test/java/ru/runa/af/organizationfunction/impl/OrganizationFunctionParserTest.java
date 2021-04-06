package ru.runa.af.organizationfunction.impl;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;

/**
 * @author Gritsenko_S
 */
public class OrganizationFunctionParserTest extends ServletTestCase {
    private static final String PROCESS_PATH = "organizationProcess.par";
    private static final String PROCESS_NAME = "organizationProcess";

    private WfServiceTestHelper h;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());

        h.addExecutorToGroup(h.getHrOperator(), h.getBossGroup());
        h.deployValidProcessDefinition(PROCESS_PATH);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS), PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(PROCESS_NAME);
        h.releaseResources();
        h = null;
    }

    public void testOrganizationFunction() {
        h.getExecutionService().startProcess(h.getAuthorizedUser(), PROCESS_NAME, null);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAdminUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());
    }
}
