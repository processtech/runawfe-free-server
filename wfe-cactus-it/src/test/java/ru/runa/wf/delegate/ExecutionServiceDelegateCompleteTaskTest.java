/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateCompleteTaskTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;

    private WfTask task;
    private Map<String, Object> legalVariables;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
        // task =
        // executionDelegate.getTasks(helper.getAuthorizedUser(),
        // helper.getTaskBatchPresentation())[0];

        legalVariables = new HashMap<>();
        legalVariables.put("amount.asked", 200d);
        legalVariables.put("amount.granted", 150d);
        legalVariables.put("approved", "true");
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    private void initTask() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertTrue(tasks.size() > 0);
        task = tasks.get(0);
    }

    public void testCompleteTaskByAuthorizedUser() {
        initTask();

        assertEquals("state name differs from expected", "evaluating", task.getName());
        assertEquals("task <evaluating> is assigned before completeTask()", h.getBossGroup(), task.getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), task.getId(), legalVariables);
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());
        assertEquals("task <treating collegues on cake and pie> is not assigned after starting [requester]", h.getBossGroup(), task.getOwner());
        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), legalVariables);

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), h.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "updating erp asynchronously", tasks.get(0).getName());
        assertEquals("task <updating erp asynchronously> is not assigned before competeTask()", h.getBossGroup(), task.getOwner());
    }

    public void testCompleteTaskBySubjectWhichIsNotInSwimlane() {
        initTask();
        try {
            h.removeExecutorFromGroup(h.getAuthorizedActor(), h.getBossGroup());
            h.getTaskService().completeTask(h.getAuthorizedUser(), task.getId(), legalVariables);
            fail("testCompleteTaskByNullSubject(), no Exception");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testCompleteTaskByUnauthorizedUser() {
        initTask();
        try {
            h.getTaskService().completeTask(h.getUnauthorizedUser(), task.getId(), legalVariables);
            fail("testCompleteTaskByNullSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testCompleteTaskByFakeUser() {
        initTask();
        try {
            h.getTaskService().completeTask(h.getFakeUser(), task.getId(), legalVariables);
            fail("expected AuthenticationException");
        } catch (AuthorizationException e) {
            fail("expected AuthenticationException");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testCompleteTaskByAuthorizedUserWithInvalidTaskId() {
        initTask();
        try {
            h.getTaskService().completeTask(h.getAuthorizedUser(), -1L, legalVariables);
            fail();
        } catch (TaskDoesNotExistException e) {
            // Expected.
        }
    }
}
