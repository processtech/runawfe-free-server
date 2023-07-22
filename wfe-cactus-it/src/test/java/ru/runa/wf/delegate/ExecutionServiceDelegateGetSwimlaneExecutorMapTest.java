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
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetSwimlaneExecutorMapTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Long instanceId;
    private HashMap<String, Object> legalVariables;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ, Permission.READ_PROCESS),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        // instanceId =
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
        instanceId = executionService.getProcesses(h.getAdminUser(), h.getProcessInstanceBatchPresentation()).get(0).getId();

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

    public void testGetSwimlaneExecutorMapByUnauthorizedUser() {
        try {
            h.getTaskService().getProcessTasks(h.getUnauthorizedUser(), instanceId, true);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetSwimlaneExecutorMapByFakeUser() {
        try {
            h.getTaskService().getProcessTasks(h.getFakeUser(), instanceId, true);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedUserWithInvalidProcessId() {
        try {
            h.getTaskService().getProcessTasks(h.getAuthorizedUser(), -1L, true);
            fail();
        } catch (ProcessDoesNotExistException e) {
            // Expected.
        }
    }

    //
    // public void testGetSwimlaneExecutorMapByAuthorizedUser() throws
    // Exception {
    // Collection<Permission> readPermissions =
    // Lists.newArrayList(Permission.READ);
    // helper.setPermissionsToAuthorizedActor(readPermissions,
    // helper.getErpOperator());
    //
    // List<Swimlane> swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedUser(),
    // instanceId);
    //
    // swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedUser(),
    // instanceId);
    // for (Swimlane swimlane : swimlanes) {
    // Map<String, Executor> executorsInSwimlane =
    // executionService.getActiveTasks(helper.getAuthorizedUser(),
    // instanceId);
    // for (String name : executorsInSwimlane.keySet()) {
    // Assert.assertEquals("Executor in the swimlane differs from expected",
    // getExpectedExecutor(swimlane), executorsInSwimlane.get(name));
    // }
    // }
    //
    // WfTask task =
    // h.getTaskService().getMyTasks(helper.getAuthorizedUser(),
    // helper.getTaskBatchPresentation()).get(0);
    // h.getTaskService().completeTask(helper.getAuthorizedUser(),
    // task.getId(), legalVariables);
    //
    // swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedUser(),
    // instanceId);
    // for (Swimlane swimlane : swimlanes) {
    // Map<String, Executor> executorsInSwimlane =
    // executionService.getActiveTasks(helper.getAuthorizedUser(),
    // instanceId,
    // swimlane.getDefinition().getName());
    // for (String name : executorsInSwimlane.keySet()) {
    // Assert.assertEquals("Executor in the swimlane differs from expected",
    // getExpectedExecutor(swimlane), executorsInSwimlane.get(name));
    // }
    // }
    // }
    //
    // public void testGetSwimlaneExecutorMapDeletedExecutor()
    // {
    // WfTask task =
    // h.getTaskService().getMyTasks(helper.getAuthorizedUser(),
    // helper.getTaskBatchPresentation()).get(0);
    // h.getTaskService().completeTask(helper.getAuthorizedUser(),
    // task.getId(), legalVariables);
    // List<Swimlane> swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedUser(),
    // instanceId);
    // Swimlane swimlane = null;
    // for (Swimlane existing : swimlanes) {
    // if ("erp operator".equals(existing.getDefinition().getName())) {
    // swimlane = existing;
    // break;
    // }
    // }
    // assert (swimlane != null);
    // helper.removeCreatedExecutor(helper.getErpOperator());
    // helper.removeExecutorIfExists(helper.getErpOperator());
    // try {
    // executionService.getActiveTasks(helper.getAuthorizedUser(),
    // instanceId, swimlane.getDefinition().getName());
    // fail("executionDelegate.getSwimlaneExecutorMap() does not throw exception for getting swimlane for nonexisting executor");
    // } catch (ExecutorDoesNotExistException e) {
    // }
    // }

    private Executor getExpectedExecutor(WfSwimlane WfSwimlane) {
        String name = WfSwimlane.getDefinition().getName();
        switch (name) {
            case "requester":
                return h.getAuthorizedActor();
            case "boss":
                return h.getBossGroup();
            case "erp operator":
                return h.getErpOperator();
            default:
                throw new RuntimeException("Executor for swimlane " + WfSwimlane.getDefinition().getName() + " is unknown");
        }
    }
}
