package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.apache.cactus.ServletTestCase;
import org.hibernate.TransientObjectException;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 */
public class ExecutionServiceDelegateGetTasksTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        batchPresentation = h.getTaskBatchPresentation();

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);
        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
        batchPresentation = null;
    }

    public void testGetTasksByAuthorizedUser() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "evaluating", tasks.get(0).getName());
        assertEquals("task <evaluating> is assigned before completeTask()", h.getBossGroup(), tasks.get(0).getOwner());

        Map<String, Object> variables = WfServiceTestHelper.createVariablesMap("approved", "true");
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), variables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());
        assertEquals("task <treating collegues on cake and pie> is not assigned after starting [requester]", h.getAuthorizedActor(),
                tasks.get(0).getOwner());
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), variables);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "updating erp asynchronously", tasks.get(0).getName());
        assertEquals("task <updating erp asynchronously> is not assigned before competeTask()", h.getErpOperator(), tasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getErpOperatorUser(), tasks.get(0).getId(), variables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Tasks are returned for Authorized Subject", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "notify", tasks.get(0).getName());
        assertEquals("task <notify> is in assigned swimlane", h.getErpOperator(), tasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getErpOperatorUser(), tasks.get(0).getId(), variables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("Tasks are returned for Authorized Subject", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), batchPresentation);
        assertEquals("Tasks are returned for Erp Operator Subject", 0, tasks.size());
    }

    public void testGetTasksByVariableFilterByAuthorizedUserWithExactMatch() {
        Long proc1 = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var1", "var1Value"));
        Long proc2 = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var2", "var2Value"));
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].variablePrototype) {
                batchPresentation.addDynamicField(i, "var1");
            }
        }
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria("var1Value"));
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals(1, tasks.size());

        h.setPermissionsToAuthorizedActorOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(h.getAdminUser(), proc1));
        h.setPermissionsToAuthorizedActorOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(h.getAdminUser(), proc2));

        List<WfVariable> variables = executionService.getVariables(h.getAuthorizedUser(), tasks.get(0).getProcessId());
        assertEquals("var1Value", variables.get(0).getValue());
    }

    public void testGetTasksByVariableFilterByAuthorizedUserWithContainsMatch() {
        Long proc1 = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var1", "var1Value"));
        Long proc2 = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var2", "var2Value"));
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].variablePrototype) {
                batchPresentation.addDynamicField(i, "var1");
            }
        }
        batchPresentation.getFilteredFields().put(0, new AnywhereStringFilterCriteria("1Val"));
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals(1, tasks.size());

        h.setPermissionsToAuthorizedActorOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(h.getAdminUser(), proc1));
        h.setPermissionsToAuthorizedActorOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(h.getAdminUser(), proc2));

        List<WfVariable> variables = executionService.getVariables(h.getAuthorizedUser(), tasks.get(0).getProcessId());
        assertEquals("var1Value", variables.get(0).getValue());
    }

    public void testGetTasksByUnauthorizedUser() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getUnauthorizedUser(), batchPresentation);
        assertEquals("Tasks returned for Unauthorized Subject", 0, tasks.size());
    }

    public void testGetTasksByFakeUser() {
        try {
            h.getTaskService().getMyTasks(h.getFakeUser(), batchPresentation);
            fail("expected AuthenticationException");
        } catch (TransientObjectException e) {
            fail("expected AuthenticationException");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
