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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateStartProcessInstanceWithMapTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Map<String, Object> startVariables;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        helper.createDefaultExecutorsMap();
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(DefinitionPermission.READ, DefinitionPermission.UPDATE_PERMISSIONS,
                DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        Collection<Permission> executorPermission = Lists.newArrayList(ExecutorPermission.READ);
        helper.setPermissionsToAuthorizedPerformer(executorPermission, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorPermission, helper.getSubGroupActor());

        startVariables = new HashMap<String, Object>();
        startVariables.put("var1start", "var1Value");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testStartProcessInstanceWithMapByUnauthorizedSubject() throws Exception {
        try {
            executionService.startProcess(helper.getUnauthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, startVariables);
            fail("testStartProcessInstanceWithMapByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testStartProcessInstanceWithMapByFakeSubject() throws Exception {
        try {
            executionService.startProcess(helper.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, startVariables);
            fail("testStartProcessInstanceWithMapByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testStartProcessInstanceWithMapByNullSubject() throws Exception {
        try {
            executionService.startProcess(null, WfServiceTestHelper.VALID_PROCESS_NAME, null, startVariables);
            fail("testStartProcessInstanceWithMapByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedSubjectWithInvalidProcessDefinitionName() throws Exception {
        try {
            executionService.startProcess(helper.getAuthorizedPerformerUser(), "INVALID_PROCESS_NAME", null, startVariables);
            assertTrue("testStartProcessInstanceWithMapByAuthorizedSubjectWithInvalidProcessDefinitionName(), no DefinitionDoesNotExistException",
                    false);
        } catch (DefinitionDoesNotExistException e) {
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedSubjectWithNullVariables() throws Exception {
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, null);
    }

    public void testStartProcessInstanceWithMapByAuthorizedSubject() throws Exception {
        Long processId = executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, startVariables);

        List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(),
                helper.getProcessInstanceBatchPresentation());
        assertEquals("Process not started", 1, processInstances.size());

        List<WfVariable> variables = executionService.getVariables(helper.getAuthorizedPerformerUser(), processId);

        HashMap<String, Object> actualVariables = new HashMap<String, Object>();
        for (WfVariable v : variables) {
            actualVariables.put(v.getDefinition().getName(), v.getValue());
        }

        for (Map.Entry<String, Object> entry : startVariables.entrySet()) {
            assertEquals("No predefined variable", actualVariables.get(entry.getKey()), entry.getValue());
        }
        // TODO assertEquals("No swimlane variable",
        // String.valueOf(helper.getAuthorizedPerformerActor().getCode()),
        // actualVariables.get("requester"));
    }

    public void testStartProcessInstanceWithMapInstancePermissions() throws Exception {
        WfDefinition defintiion = helper.getDefinitionService().getLatestProcessDefinition(helper.getAuthorizedPerformerUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.READ_STARTED_PROCESS);
        helper.getAuthorizationService().setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), permissions,
                defintiion);
        permissions = Lists.newArrayList(DefinitionPermission.READ_STARTED_PROCESS, DefinitionPermission.CANCEL_STARTED_PROCESS);
        helper.getAuthorizationService().setPermissions(helper.getAuthorizedPerformerUser(), helper.getSubGroupActor().getId(), permissions,
                defintiion);

        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null, startVariables);

        helper.getExecutionService().getProcesses(helper.getAuthorizedPerformerUser(), helper.getProcessInstanceBatchPresentation());

        WfProcess instance = getInstance(WfServiceTestHelper.VALID_PROCESS_NAME);
        Collection<Permission> actual = helper.getAuthorizationService().getIssuedPermissions(helper.getAuthorizedPerformerUser(),
                helper.getBaseGroupActor(), instance);
        Collection<Permission> expected = Lists.newArrayList(ProcessPermission.READ);
        ArrayAssert.assertWeakEqualArrays("startProcessInstance() does not grant permissions on instance", expected, actual);
        actual = helper.getAuthorizationService().getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getSubGroupActor(), instance);
        expected = Lists.newArrayList(ProcessPermission.READ, ProcessPermission.CANCEL_PROCESS);
        ArrayAssert.assertWeakEqualArrays("startProcessInstance() does not grant permissions on instance", expected, actual);
    }

    private WfProcess getInstance(String definitionName) throws InternalApplicationException {
        List<WfProcess> stubs = helper.getExecutionService().getProcesses(helper.getAuthorizedPerformerUser(),
                helper.getProcessInstanceBatchPresentation());
        for (WfProcess processInstance : stubs) {
            if (definitionName.equals(processInstance.getName())) {
                return processInstance;
            }
        }
        throw new ProcessDoesNotExistException(definitionName);
    }
}
