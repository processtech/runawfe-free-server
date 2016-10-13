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

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetProcessInstanceStubTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Long processId;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        // processId =
        executionService.startProcess(helper.getAdminUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, null);
        processId = executionService.getProcesses(helper.getAuthorizedPerformerUser(), helper.getProcessInstanceBatchPresentation()).get(0).getId();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetProcessInstanceStubByAuthorizedSubject() throws Exception {
        WfProcess processInstance = executionService.getProcess(helper.getAuthorizedPerformerUser(), processId);
        assertEquals("id of running process differs from requested", processId, processInstance.getId());
        assertEquals("name of running process differs from definition", WfServiceTestHelper.VALID_PROCESS_NAME, processInstance.getName());
    }

    public void testGetProcessInstanceStubByUnauthorizedSubject() throws Exception {
        try {
            executionService.getProcess(helper.getUnauthorizedPerformerUser(), processId);
            fail("testGetProcessInstanceStubByUnauthorizedSubject, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessInstanceStubByFakeSubject() throws Exception {
        try {
            executionService.getProcess(helper.getFakeUser(), processId);
            fail("testGetProcessInstanceStubByFakeSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessInstanceStubByNullSubject() throws Exception {
        try {
            executionService.getProcess(null, processId);
            fail("testGetProcessInstanceStubByNullSubject, no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetProcessInstanceStubByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getProcess(helper.getAuthorizedPerformerUser(), -1l);
            fail("testGetProcessInstanceStubByAuthorizedSubjectWithInvalidProcessId, no ProcessInstanceDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
        }
    }
}
