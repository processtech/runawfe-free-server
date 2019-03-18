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

import java.util.ArrayList;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateCancelProcessInstanceTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private WfProcess processInstance = null;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);

        batchPresentation = helper.getProcessInstanceBatchPresentation();

        processInstance = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation).get(0);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testCancelProcessInstanceByAuthorizedSubject() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(Permission.CANCEL), processInstance);
        executionService.cancelProcess(helper.getAuthorizedPerformerUser(), processInstance.getId());

        List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Process instance does not exist", 1, processInstances.size());
        assertEquals("Process not cancelled", ExecutionStatus.ENDED, processInstances.get(0).getExecutionStatus());
    }

    public void testCancelProcessInstanceByAuthorizedSubjectWithoutCANCELPermission() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(Permission.READ), processInstance);
        try {
            executionService.cancelProcess(helper.getAuthorizedPerformerUser(), processInstance.getId());
            List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
            assertEquals("Process instance does not exist", 1, processInstances.size());
            assertEquals("Process was canceled without CANCEL permission", ExecutionStatus.ACTIVE, processInstances.get(0).getExecutionStatus());
        } catch (AuthorizationException e) {
        }
    }

    public void testCancelProcessInstanceByFakeSubject() throws Exception {
        try {
            executionService.cancelProcess(helper.getFakeUser(), processInstance.getId());
            fail("executionDelegate.cancelProcessInstance(helper.getFakeUser(), ..), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testCancelProcessInstanceByUnauthorizedSubject() throws Exception {
        try {
            executionService.cancelProcess(helper.getUnauthorizedPerformerUser(), processInstance.getId());
            List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
            assertEquals("Process was cancelled by unauthorized subject", ExecutionStatus.ACTIVE, processInstances.get(0).getExecutionStatus());
        } catch (AuthorizationException e) {
        }
    }
}
