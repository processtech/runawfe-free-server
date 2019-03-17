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

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
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
public class ExecutionServiceDelegateStartProcessInstanceTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(Permission.START, Permission.READ_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testStartProcessInstanceByAuthorizedSubject() throws Exception {
        Long processInstanceId = executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        assertNotNull(processInstanceId);
        List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(),
                helper.getProcessInstanceBatchPresentation());
        assertEquals("Process not started", 1, processInstances.size());
        assertEquals(processInstanceId, processInstances.get(0).getId());
    }

    public void testStartProcessInstanceByUnauthorizedSubject() throws Exception {
        try {
            executionService.startProcess(helper.getUnauthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testStartProcessInstanceByUnauthorizedSubject, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testStartProcessInstanceByFakeSubject() throws Exception {
        try {
            executionService.startProcess(helper.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testStartProcessInstanceByFakeSubject, no AuthenticationException");
        } catch (InvalidDataAccessApiUsageException e) {
            fail("testStartProcessInstanceByFakeSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testStartProcessInstanceByAuthorizedSubjectWithoutSTARTPermission() throws Exception {
        Collection<Permission> noPermissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(noPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testStartProcessInstanceByAuthorizedSubjectWithoutSTARTPermission, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testStartProcessInstanceByAuthorizedSubjectWithoutREADPermission() throws Exception {
        Collection<Permission> startPermissions = Lists.newArrayList(Permission.START);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        List<WfProcess> processInstances = executionService.getProcesses(helper.getAdminUser(), helper.getProcessInstanceBatchPresentation());
        assertEquals(1, processInstances.size());
    }

    public void testStartProcessInstanceByAuthorizedSubjectWithInvalidProcessName() throws Exception {
        try {
            executionService.startProcess(helper.getAuthorizedPerformerUser(), "0_INVALID_PROCESS_NAME", null);
            fail("executionDelegate.startProcessInstance(subj, invalid name), no DefinitionDoesNotExistException");
        } catch (DefinitionDoesNotExistException e) {
        }
    }
}
