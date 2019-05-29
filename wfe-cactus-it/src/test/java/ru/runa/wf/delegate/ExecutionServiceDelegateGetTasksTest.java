package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;
import org.hibernate.TransientObjectException;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 */
public class ExecutionServiceDelegateGetTasksTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        th.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(Permission.START);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        batchPresentation = th.getTaskBatchPresentation();

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), th.getBossGroup());

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetTasksByAuthorizedSubject() throws Exception {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "evaluating", tasks.get(0).getName());
        assertEquals("task <evaluating> is assigned before completeTask()", th.getBossGroup(), tasks.get(0).getOwner());

        Map<String, Object> variables = WfServiceTestHelper.createVariablesMap("approved", "true");
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), variables);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());
        assertEquals("task <treating collegues on cake and pie> is not assigned after starting [requester]", th.getAuthorizedPerformerActor(),
                tasks.get(0).getOwner());
        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), variables);

        tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "updating erp asynchronously", tasks.get(0).getName());
        assertEquals("task <updating erp asynchronously> is not assigned before competeTask()", th.getErpOperator(), tasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getErpOperatorUser(), tasks.get(0).getId(), variables);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Tasks are returned for Authorized Subject", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "notify", tasks.get(0).getName());
        assertEquals("task <notify> is in assigned swimlane", th.getErpOperator(), tasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getErpOperatorUser(), tasks.get(0).getId(), variables);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Tasks are returned for Authorized Subject", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), batchPresentation);
        assertEquals("Tasks are returned for Erp Operator Subject", 0, tasks.size());
    }

    public void testGetTasksByVariableFilterByAuthorizedSubjectWithExactMatch() throws Exception {
        Long proc1 = executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var1", "var1Value"));
        Long proc2 = executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var2", "var2Value"));
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].displayName.startsWith(ClassPresentation.editable_prefix)) {
                batchPresentation.addDynamicField(i, "var1");
            }
        }
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria("var1Value"));
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals(1, tasks.size());

        th.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(th.getAdminUser(), proc1));
        th.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(th.getAdminUser(), proc2));

        List<WfVariable> variables = executionService.getVariables(th.getAuthorizedPerformerUser(), tasks.get(0).getProcessId());
        assertEquals("var1Value", variables.get(0).getValue());
    }

    public void testGetTasksByVariableFilterByAuthorizedSubjectWithContainsMatch() throws Exception {
        Long proc1 = executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var1", "var1Value"));
        Long proc2 = executionService.startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME,
                WfServiceTestHelper.createVariablesMap("var2", "var2Value"));
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].displayName.startsWith(ClassPresentation.editable_prefix)) {
                batchPresentation.addDynamicField(i, "var1");
            }
        }
        batchPresentation.getFilteredFields().put(0, new AnywhereStringFilterCriteria("1Val"));
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals(1, tasks.size());

        th.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(th.getAdminUser(), proc1));
        th.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(Permission.READ),
                executionService.getProcess(th.getAdminUser(), proc2));

        List<WfVariable> variables = executionService.getVariables(th.getAuthorizedPerformerUser(), tasks.get(0).getProcessId());
        assertEquals("var1Value", variables.get(0).getValue());
    }

    public void testGetTasksByUnauthorizedSubject() throws Exception {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getUnauthorizedPerformerUser(), batchPresentation);
        assertEquals("Tasks returned for Unauthorized Subject", 0, tasks.size());
    }

    public void testGetTasksByFakeSubject() throws Exception {
        try {
            th.getTaskService().getMyTasks(th.getFakeUser(), batchPresentation);
            fail("testGetTasksByFakeSubject(), no AuthenticationException");
        } catch (TransientObjectException e) {
            fail("testGetTasksByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

}
